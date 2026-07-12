# Vehicle Sales Service

Este é o microserviço de Vendas de Veículos (`vehicle-sales-service`), projetado para suportar altos picos de tráfego de maneira isolada. O serviço foi estruturado utilizando **Arquitetura Hexagonal (Clean/Ports and Adapters)** em Java 21 com Spring Boot.

---

## 1. Responsabilidade do Serviço

O serviço gerencia dois agregados principais:
- **ItemCatalogo**: Uma réplica local dos dados de veículos do catálogo de anúncios (vitrine). Mantido sincronizado via push HTTP vindo do catálogo.
- **Venda**: Registro de tentativa de compra de um veículo. Possui ciclo de vida independente do ItemCatalogo.

---

## 2. Premissas de Integração e Design

### Tratamento de Condições de Corrida (Race Conditions)
* Ao consultar se um veículo já foi vendido via `GET /veiculos/{id}/vendido` (implementado por [VerificarVeiculoVendidoUseCase](file:///home/isadmot/Github/VendasService/src/main/java/br/com/fiap/sout/vendas/application/usecases/VerificarVeiculoVendidoUseCase.java)), caso o `veiculoId` consultado não exista localmente na tabela `tb_itens_catalogo`, o serviço retornará `false`.
* **Premissa de negócio**: Se o veículo ainda não foi sincronizado com este serviço (por exemplo, devido a uma latência de rede logo após a criação no catálogo), ele certamente ainda não pôde ser vendido por aqui. Responder `false` permite que o catálogo continue liberando edições e publicações com segurança.

### Garantia de Idempotência e Concorrência de Webhooks
* A confirmação ou cancelamento da venda no webhook de pagamentos (`POST /pagamentos/webhook`) é feita através de um update condicional atômico (`status = 'PENDENTE_PAGAMENTO'`).
* Caso a requisição seja duplicada e a venda já tenha sido processada (status `CONFIRMADA` ou `CANCELADA`), o banco de dados retornará 0 linhas afetadas. O serviço trata isso como um cenário de **idempotência de sucesso** e retorna HTTP 200 sem disparar novos updates ou erros ao cliente.

---

## 3. Arquitetura do Projeto

O projeto segue a seguinte estrutura de pacotes:

- `domain`: Modelos de domínio puros, enums e exceções de negócio.
- `application.ports`: Interfaces de comunicação do core:
  - `in`: Interfaces chamadas pelos adapters de entrada (controllers e scheduler).
  - `out`: Interfaces chamadas pelo domínio (persistência).
- `application.usecases`: Casos de uso concretos que implementam os ports de entrada.
- `adapter.in.web`: Controllers REST, DTOs e mappers de entrada.
- `adapter.out.persistence`: Entidades JPA, mappers MapStruct e repositórios JPA.
- `infra`: Filtros de segurança (JWT/HMAC), configurações de beans e schedulers.

---

## 4. Modelagem de Dados

### Tabela `tb_itens_catalogo`
Réplica local do catálogo usada como vitrine performática.

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | UUID | Primary Key | Gerado localmente por este serviço |
| `veiculo_id` | UUID | Not Null | Referência ao veículo no catálogo (sem FK real) |
| `marca` | VARCHAR | Not Null | Marca do veículo |
| `modelo` | VARCHAR | Not Null | Modelo do veículo |
| `ano` | INT | Not Null | Ano do modelo do veículo |
| `cor` | VARCHAR | Not Null | Cor do veículo |
| `preco` | NUMERIC(19,2) | Not Null, Index | Preço (indexado para listagem rápida) |
| `placa` | VARCHAR(8) | Not Null, Unique | Placa do veículo |
| `status` | VARCHAR | Enum: `DISPONIVEL`, `RESERVADO`, `VENDIDO` | Status atual do item |

### Tabela `tb_vendas`
Registra a intenção de compra e as reservas.

| Campo | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | UUID | Primary Key | ID da venda |
| `item_catalogo_id` | UUID | FK -> `tb_itens_catalogo(id)` | Referência ao item reservado/vendido |
| `cpf_comprador` | VARCHAR | Not Null | CPF do comprador |
| `data_venda` | TIMESTAMP | Not Null | Data e hora em que a reserva foi feita |
| `codigo_pagamento` | VARCHAR | Not Null, Unique | Código gerado para identificação do pagamento |
| `status` | VARCHAR | Enum: `PENDENTE_PAGAMENTO`, `CONFIRMADA`, `CANCELADA` | Estado da venda |
| `expira_em` | TIMESTAMP | Not Null | Data e hora limite para expiração da reserva |

---

## 5. Endpoints da API

A documentação interativa da API está disponível via **Swagger UI** (quando a aplicação está rodando) em:
- [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

### Item Catalogo (Veículos)
- `POST /veiculos` (Autenticado via **JWT M2M**)
  - Sincroniza dados enviados pelo catálogo.
- `GET /veiculos` (Público)
  - Retorna veículos `DISPONIVEL` paginados e ordenados por preço ASC.
- `GET /veiculos/vendidos` (Público)
  - Retorna veículos `VENDIDO` paginados e ordenados por preço ASC.
- `GET /veiculos/{id}/vendido` (Autenticado via **JWT M2M**)
  - Verifica pontualmente se um veículo (`veiculoId`) já foi vendido.

### Vendas
- `POST /vendas` (Público)
  - Efetua venda do veículo através de um comando contendo CPF e ID do item. Reserva o veículo e gera o `codigoPagamento` da venda.

### Webhook de Pagamentos
- `POST /pagamentos/webhook` (Autenticado via **HMAC-SHA256**)
  - Recebe notificações do processador de pagamentos (`status: APROVADO` ou `CANCELADO`).
  - Header obrigatório: `X-Signature` contendo o hash HMAC-SHA256 do payload JSON.

---

## 6. Segurança

O serviço utiliza duas cadeias de autenticação distintas:

1. **JWT M2M (Machine-to-Machine)**:
   - Validado no cabeçalho `Authorization: Bearer <token>` em rotas internas do catálogo (`POST /veiculos` e `GET /veiculos/{id}/vendido`).
   - A chave secreta (`jwt.secret`) é compartilhada com o serviço do catálogo.
   - > [!IMPORTANT]
     > **Requisito de Configuração em Produção**: O segredo definido na propriedade `jwt.secret` (variável de ambiente `JWT_SECRET`) **deve ser idêntico** ao segredo configurado no catálogo de veículos. Ambos os serviços precisam compartilhar da mesma chave para que o JWT gerado pelo catálogo seja validado com sucesso pelo serviço de vendas.

2. **HMAC-SHA256 (Webhook)**:
   - Validado no cabeçalho `X-Signature` no endpoint `/pagamentos/webhook`.
   - O filtro recalculada o HMAC do corpo da requisição bruta usando `hmac.secret` e valida a integridade/autoridade do envio.

---

## 7. Como Executar Localmente

### Passo 1: Subir o banco de dados PostgreSQL
Suba o container PostgreSQL mapeado na porta `5433` (para não colidir com o catálogo):
```bash
docker-compose up -d
```

### Passo 2: Executar a aplicação
Você pode executar o projeto Spring Boot com o Maven:
```bash
./mvnw spring-boot:run
```
O Liquibase aplicará as migrations e o banco `db_vendas` será seedado automaticamente com dados de teste.

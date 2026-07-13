# Vendas Veículo Service

Microsserviço de vendas de veículos da plataforma de revenda automotiva. Projetado para suportar altos picos de tráfego de forma isolada, gerencia a vitrine de veículos disponíveis, o fluxo de compra e o webhook de confirmação de pagamento.

---

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como rodar localmente](#como-rodar-localmente)
- [Como executar os testes](#como-executar-os-testes)
- [Documentação da API](#documentação-da-api)
- [Testando o webhook de pagamento](#testando-o-webhook-de-pagamento)
- [Segurança](#segurança)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Decisões de escopo](#decisões-de-escopo)

---

## Visão geral

Este serviço mantém uma **réplica local** dos dados de veículos recebidos do `veiculo-catalogo-service` via push HTTP, e gerencia todo o fluxo de venda: reserva atômica (sem race condition), geração de código de pagamento, recebimento do webhook da processadora e expiração automática de reservas órfãs.

**Responsabilidades:**
- Receber e armazenar a réplica local dos veículos (sincronização push do catálogo)
- Listar veículos disponíveis e vendidos (paginado, ordenado por preço)
- Efetuar venda com reserva atômica via update condicional
- Receber webhook de pagamento com validação HMAC-SHA256 e idempotência garantida
- Expirar reservas órfãs automaticamente via scheduler
- Responder ao catálogo se um veículo já foi vendido

> **Este serviço não tem Terraform nem manifests Kubernetes próprios.** Toda a infraestrutura é provisionada pelo `veiculo-catalogo-service`. Para o ambiente Kubernetes completo, siga o README daquele repositório.

---

## Arquitetura

```
vendas-veiculo-service/
├── src/main/java/.../vendas/
│   ├── domain/
│   │   ├── model/       → ItemCatalogo, Venda (records imutáveis)
│   │   ├── enums/       → StatusItemCatalogo, StatusVenda, StatusPagamento
│   │   └── exceptions/  → Exceções de negócio
│   ├── application/
│   │   ├── ports/in/    → Interfaces dos casos de uso (entrada)
│   │   ├── ports/out/   → Interfaces de repositório (saída)
│   │   └── usecases/    → Lógica de negócio pura (sem Spring)
│   ├── adapter/
│   │   ├── in/web/      → Controllers REST, DTOs, mappers, exception handler
│   │   └── out/persistence/ → Entidades JPA, repositórios, mappers MapStruct
│   └── infra/
│       ├── config/      → UseCaseConfig, OpenApiConfig, SecurityConfig
│       ├── security/    → JwtAuthenticationFilter, HmacSignatureFilter
│       └── scheduler/   → Job de expiração de reservas órfãs
└── docker-compose.yml   → PostgreSQL local (porta 5433)
```

**Padrão:** Arquitetura Hexagonal (Ports & Adapters)  
**Stack:** Java 21, Spring Boot 4.1.0, Spring Security, PostgreSQL 15, Liquibase, MapStruct

---

## Pré-requisitos

| Ferramenta | Versão mínima | Instalação |
|---|---|---|
| Java | 21 | [Temurin](https://adoptium.net/) |
| Maven | 3.9+ | [maven.apache.org](https://maven.apache.org/) |
| Docker + Docker Compose | 24+ | [docker.com](https://www.docker.com/) |

> Para rodar com Kubernetes, siga o README do `veiculo-catalogo-service`.

---

## Como rodar localmente

O banco deste serviço usa a **porta 5433** para não conflitar com o `veiculo-catalogo-service` (porta 5432).

### Passo 1 — Clone o repositório

```bash
git clone https://github.com/<seu-usuario>/vendas-veiculo-service.git
cd vendas-veiculo-service
```

### Passo 2 — Suba o banco de dados

```bash
docker compose up -d
```

### Passo 3 — Execute a aplicação

```bash
./mvnw spring-boot:run
```

O Liquibase aplicará as migrations e seedará o banco com dados de teste automaticamente. A aplicação ficará disponível em `http://localhost:8081`.

### Rodando os dois serviços juntos (fluxo completo)

**Terminal 1 — catálogo:**
```bash
cd veiculo-catalogo-service
docker compose up -d postgres-catalogo
./mvnw spring-boot:run
```

**Terminal 2 — vendas:**
```bash
cd vendas-veiculo-service
docker compose up -d
./mvnw spring-boot:run
```

---

## Como executar os testes

```bash
./mvnw verify
```
Relatório de cobertura (JaCoCo):
```
target/site/jacoco/index.html
```

| Tipo | O que testa | Ferramenta |
|---|---|---|
| Unitário | Use cases (lógica de negócio pura) | JUnit 5 + Mockito |
| Slice | Controllers REST + filtros de segurança | `@WebMvcTest` + MockMvc |
| Integração | Fluxo completo (sincronizar → vender → webhook → confirmar) | `@SpringBootTest` + Testcontainers |

---

## Documentação da API

- **Swagger UI:** http://localhost:8081/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8081/v3/api-docs

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| `POST` | `/veiculos` | Recebe sincronização do catálogo | JWT M2M |
| `GET` | `/veiculos` | Lista veículos disponíveis (paginado, ordem por preço) | Pública |
| `GET` | `/veiculos/vendidos` | Lista veículos vendidos (paginado, ordem por preço) | Pública |
| `GET` | `/veiculos/{id}/vendido` | Verifica se veículo foi vendido | JWT M2M |
| `POST` | `/vendas` | Efetua venda (reserva + código de pagamento) | Pública |
| `POST` | `/pagamentos/webhook` | Confirma/cancela pagamento | HMAC-SHA256 |

**Paginação:**
```
GET /veiculos?page=0&size=10
GET /veiculos/vendidos?page=0&size=20
```

---

## Testando o webhook de pagamento

O endpoint `POST /pagamentos/webhook` exige o header `X-Signature` com o HMAC-SHA256 do corpo da requisição. Use o comando abaixo para calcular a assinatura e enviar a requisição:

```bash
BODY='{"codigoPagamento":"SEU_CODIGO_DE_PAGAMENTO","status":"APROVADO"}'
SIG=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "super-secret-hmac-signature-key-for-webhook-validation-2026" | awk '{print $2}')

curl -X POST http://localhost:8081/pagamentos/webhook \
  -H "Content-Type: application/json" \
  -H "X-Signature: $SIG" \
  -d "$BODY"
```

Para cancelar o pagamento, use `"status":"CANCELADO"` no body.

> **Segredo HMAC local:** `super-secret-hmac-signature-key-for-webhook-validation-2026`  
> Este é o valor configurado no Secret Kubernetes (`k8s/vendas/secret.yaml`). Em produção, este segredo deve ser diferente e compartilhado com a processadora de pagamentos.

### Fluxo completo de teste via curl

```bash
# 1. Cadastra veículo no catálogo
curl -X POST http://localhost:8080/veiculos \
  -H "Content-Type: application/json" \
  -d '{"marca":"Toyota","modelo":"Corolla","ano":2023,"cor":"Prata","preco":150000.00,"placa":"TST1A23"}'

# 2. Lista veículos disponíveis em vendas (aguarda a sincronização)
curl http://localhost:8081/veiculos

# 3. Efetua venda (substitua o itemCatalogoId pelo id retornado no passo 2)
curl -X POST http://localhost:8081/vendas \
  -H "Content-Type: application/json" \
  -d '{"cpfComprador":"123.456.789-00","itemCatalogoId":"ID_DO_ITEM_AQUI"}'

# 4. Confirma o pagamento via webhook (substitua o codigoPagamento pelo retornado no passo 3)
BODY='{"codigoPagamento":"CODIGO_AQUI","status":"APROVADO"}'
SIG=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "super-secret-hmac-signature-key-for-webhook-validation-2026" | awk '{print $2}')
curl -X POST http://localhost:8081/pagamentos/webhook \
  -H "Content-Type: application/json" \
  -H "X-Signature: $SIG" \
  -d "$BODY"

# 5. Confirma que o veículo aparece como VENDIDO
curl http://localhost:8081/veiculos/vendidos
```

---

## Segurança

### JWT M2M

Protege rotas internas chamadas pelo `veiculo-catalogo-service`:
- `POST /veiculos` e `GET /veiculos/{id}/vendido`

Header: `Authorization: Bearer <token>`

Token ausente → `400 Bad Request`. Token inválido → `403 Forbidden`.

> O segredo JWT (`jwt.secret`) deve ser **idêntico** ao configurado no `veiculo-catalogo-service`.

### HMAC-SHA256

Protege o webhook de pagamento:
- `POST /pagamentos/webhook`

Header: `X-Signature: <hmac-sha256-do-corpo-em-hex>`

Assinatura ausente → `400 Bad Request`. Assinatura incorreta → `403 Forbidden`.

---

## Variáveis de ambiente

| Variável | Padrão (local) | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do banco |
| `DB_PORT` | `5433` | Porta do banco |
| `DB_NAME` | `db_vendas` | Nome do banco |
| `DB_USERNAME` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `JWT_SECRET` | *(ver application.yaml)* | Segredo JWT compartilhado com o catálogo |
| `HMAC_SECRET` | `super-secret-hmac-signature-key-for-webhook-validation-2026` | Segredo HMAC do webhook |
| `RESERVA_EXPIRACAO_MINUTOS` | `10` | Tempo de expiração da reserva |
| `RESERVA_EXPIRACAO_INTERVALO_MS` | `60000` | Intervalo do scheduler (ms) |

---
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
| `POST` | `/veiculos` | Recebe sincronização do catálogo *(oculta no Swagger, ver nota abaixo)* | JWT M2M |
| `GET` | `/veiculos` | Lista veículos disponíveis (paginado, ordem por preço) | Pública |
| `GET` | `/veiculos/vendidos` | Lista veículos vendidos (paginado, ordem por preço) | Pública |
| `GET` | `/veiculos/{id}/vendido` | Verifica se veículo foi vendido *(oculta no Swagger, ver nota abaixo)* | JWT M2M |
| `POST` | `/vendas` | Efetua venda (reserva + código de pagamento) | Pública |
| `POST` | `/pagamentos/webhook` | Confirma/cancela pagamento | HMAC-SHA256 |
| `POST` | `/auth/token` | *(perfil `dev`)* Gera um JWT M2M para testar as rotas acima no Swagger | Pública |
| `POST` | `/auth/hmac-signature` | *(perfil `dev`)* Gera corpo canônico + assinatura para testar o webhook no Swagger | Pública |

> **Rotas ocultas do Swagger:** `POST /veiculos` e `GET /veiculos/{id}/vendido` são chamadas apenas pelo `veiculo-catalogo-service` (M2M), por isso têm `@Hidden` e não aparecem na UI — mas continuam funcionando normalmente para quem tiver o token. Use `POST /auth/token` para gerar um token de teste e chame-as diretamente via curl/Postman.

> **⚠️ Cuidado com `id` x `veiculoId`:** os itens do catálogo retornados por `GET /veiculos` trazem dois identificadores parecidos: `id` (chave interna deste serviço, gerada aqui) e `veiculoId` (o ID do veículo no `veiculo-catalogo-service`). **Todas as rotas voltadas a quem consome a API — `POST /vendas`, `GET /veiculos/{id}/vendido` — usam sempre o `veiculoId`, nunca o `id`.** Confundir os dois é a causa mais comum de um `409 Conflict` inesperado em `POST /vendas`.

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

### Endpoints de teste (perfil `dev`)

`POST /auth/token` e `POST /auth/hmac-signature` só existem quando o perfil Spring `dev` está ativo (`spring.profiles.active`). Eles emitem tokens JWT e assinaturas HMAC válidos **sem nenhuma autenticação real**, então servem apenas para facilitar testes manuais (Swagger/curl). Se essas rotas não aparecerem no Swagger, o perfil `dev` não está ativo — ative-o por uma das formas abaixo.

**Já vem ativo por padrão** (`spring.profiles.active: dev` no `application.yaml`), então normalmente nenhuma ação é necessária. Se precisar ativar explicitamente (por exemplo, se o padrão for removido ou sobrescrito):

- **Rodando com Maven:**
  ```bash
  SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
  ```
- **Rodando o jar diretamente:**
  ```bash
  java -jar target/VendasService-*.jar --spring.profiles.active=dev
  ```
- **Pela IDE (IntelliJ/VS Code):** na configuração de execução (*Run Configuration*), adicione a variável de ambiente `SPRING_PROFILES_ACTIVE=dev`.
- **Em Docker/Kubernetes:** defina a variável de ambiente `SPRING_PROFILES_ACTIVE=dev` no container (já configurado em `k8s/vendas/deployment.yaml`, no repositório `veiculo-catalogo-service`).

Depois de ativar, reinicie a aplicação e recarregue o Swagger (`http://localhost:8081/swagger-ui/index.html`) — `POST /auth/token` e `POST /auth/hmac-signature` devem aparecer na tag **Autenticação**.

---

## Variáveis de ambiente

| Variável | Padrão (local) | Descrição |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil ativo. Habilita `POST /auth/token` e `POST /auth/hmac-signature`. **Não usar `dev` em produção.** |
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
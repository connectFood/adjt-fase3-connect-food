# Connect Food - Fase 3

Projeto backend em arquitetura de microsservicos para o Tech Challenge da FIAP. O repositorio concentra os servicos de autenticacao, pedidos e pagamentos, com comunicacao sincrona via HTTP e assincrona via Kafka.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security com JWT
- Spring Data JPA
- Flyway
- PostgreSQL
- Apache Kafka
- Spring Cloud OpenFeign
- Resilience4j
- Swagger / OpenAPI
- Docker e Docker Compose
- Maven

## Estrutura do projeto

```text
.
├── compose.yml
├── docs/
└── services/
    ├── auth/
    ├── order/
    └── payment/
```

## Servicos

### Auth Service

Responsavel por autenticacao e emissao/renovacao de tokens JWT.

- Porta local: `8081`
- Banco/schema: `auth`
- Swagger UI: `http://localhost:8081/swagger-ui`

### Order Service

Responsavel pelo cadastro e consulta de pedidos, alem da integracao com eventos de pagamento.

- Porta local: `8082`
- Banco/schema: `order`
- Topicos Kafka: `pedido.criado`, `pagamento.aprovado`

### Payment Service

Responsavel pelo processamento de pagamentos, consumo de eventos de pedidos e integracao com o servico externo `procpag`.

- Porta local: `8083`
- Banco/schema: `payment`
- Topicos Kafka: `pedido.criado`, `pagamento.aprovado`, `pagamento.pendente`
- Dependencias adicionais: OpenFeign e Resilience4j

## Infraestrutura local

O `compose.yml` sobe os seguintes componentes:

- PostgreSQL
- Apache Kafka
- Kafka UI
- Auth Service
- Order Service
- Payment Service
- Procpag

## Como executar

### Subir tudo com Docker Compose

```bash
docker compose up --build
```

### Parar o ambiente

```bash
docker compose down
```

## Endpoints uteis

- Auth Swagger: `http://localhost:8081/swagger-ui`
- Kafka UI: `http://localhost:8080`
- Procpag: `http://localhost:8089`

## Banco de dados

Todos os servicos utilizam PostgreSQL com migracoes controladas por Flyway. Cada servico trabalha em seu proprio schema:

- `auth`
- `order`
- `payment`

## Observacoes

- As configuracoes padrao usam credenciais locais de desenvolvimento.
- O segredo JWT definido nos arquivos de configuracao deve ser trocado em ambientes reais.
- Existe uma collection Postman em `docs/collection/connectfood-fase3-e2e.postman_collection.json`.

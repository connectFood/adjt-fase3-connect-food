# Connect Food - Tech Challenge Fase 3

**Projeto:** Connect Food - Sistema de Pedido Online para Restaurante  
**Equipe:** _preencher com nomes e RMs da equipe_  
**Disciplina:** Tech Challenge - Fase 3  
**Base documental:** `docs/tc-3.md`  
**Escopo considerado neste documento:** apenas os fluxos obrigatorios da entrega (`auth-service`, `order-service` e `payment-service`)

---

## 1. Introducao

O presente documento tem como finalidade apresentar, de forma academica e estruturada, a documentacao tecnica da entrega da Fase 3 do Tech Challenge. O problema proposto consiste na implementacao de um sistema distribuido para pedidos online em restaurante, contemplando autenticacao de usuarios, criacao e consulta de pedidos, integracao com um servico externo de pagamento e mecanismos de resiliencia para tratamento de falhas.

Diferentemente de uma aplicacao monolitica tradicional, a proposta desta fase exige a divisao do problema em servicos independentes, com responsabilidades bem definidas e integrados por eventos assicronos. Nesse contexto, a solucao desenvolvida busca atender simultaneamente aos requisitos funcionais e nao funcionais descritos em `docs/tc-3.md`, com especial enfase nos seguintes pontos:

- autenticacao baseada em JWT;
- arquitetura orientada a servicos;
- mensageria com Kafka;
- persistencia relacional com PostgreSQL;
- resiliencia com Retry, Timeout e Circuit Breaker;
- reprocessamento automatico de pagamentos pendentes.

Este relatorio considera somente os elementos obrigatorios da avaliacao. Os modulos opcionais `restaurant-service` e `api-gateway` nao sao detalhados aqui, em conformidade com a orientacao da entrega.

## 2. Visao Geral

O sistema Connect Food foi concebido para representar uma base distribuida e evolutiva para operacao de pedidos online. A ideia central da solucao e desacoplar os principais contextos de negocio em componentes especializados, reduzindo dependencias diretas, melhorando a escalabilidade e favorecendo a manutencao.

Na implementacao atual, o sistema esta organizado em tres servicos obrigatorios:

- `auth-service`, responsavel pela gestao de identidade, emissoes de token e renovacao de sessao;
- `order-service`, responsavel pelo cadastro e consulta de pedidos;
- `payment-service`, responsavel pelo processamento de pagamento, tratamento de indisponibilidade da integracao externa e reprocessamento de pendencias.

Essa separacao reflete uma estrategia arquitetural coerente com sistemas modernos orientados a dominio e eventos. O `order-service` concentra o contexto de pedido, enquanto o `payment-service` encapsula o contexto de pagamento e toda a complexidade relacionada a falhas de integracao externa. O `auth-service`, por sua vez, atua como o servico central de autenticacao e emissao de JWT para os endpoints protegidos.

Em termos de experiencia de uso, o fluxo principal ocorre da seguinte forma:

1. o cliente realiza cadastro e autenticacao;
2. de posse do JWT, cria um pedido;
3. o pedido e persistido e publicado no Kafka;
4. o `payment-service` consome o evento e realiza a tentativa de cobranca;
5. se aprovado, o pagamento gera um evento de confirmacao;
6. o `order-service` consome esse evento e atualiza o pedido para `PAID`;
7. se houver falha externa, o pagamento permanece pendente e entra em fluxo automatico de reprocessamento.

## 3. Arquitetura do Sistema

### 3.1 Fundamentacao arquitetural

A estrutura do projeto segue os principios da Clean Architecture e da Hexagonal Architecture, conforme recomendado no enunciado da fase. Na pratica, isso significa que a regra de negocio central foi mantida separada dos detalhes de infraestrutura, tais como controladores REST, adaptadores de persistencia, configuracoes de mensageria e integracoes com sistemas externos.

Essa decisao traz beneficios relevantes para um projeto academico e profissional:

- maior clareza estrutural;
- separacao explicita de responsabilidades;
- facilidade de testes unitarios e de integracao;
- menor acoplamento entre regra de negocio e framework;
- maior capacidade de evolucao da solucao para fases futuras.

### 3.2 Estrutura em camadas

Os servicos estao organizados, de forma geral, nas seguintes camadas:

#### 3.2.1 Domain

E a camada central da aplicacao. Nela estao:

- entidades de dominio;
- enumeracoes de estado;
- excecoes de negocio;
- portas de entrada e saida.

Essa camada nao deve depender de tecnologias externas, preservando a independencia do nucleo de negocio.

#### 3.2.2 Application

E a camada responsavel por orquestrar os casos de uso do sistema. Nela estao:

- use cases;
- services de orquestracao;
- DTOs de entrada e saida da aplicacao;
- regras transacionais e coordenacao de chamadas entre dominio e adaptadores.

#### 3.2.3 Infrastructure

Essa camada implementa os detalhes tecnicos. Nela estao:

- controllers REST;
- adaptadores JPA;
- configuracoes do Spring;
- integracoes Kafka;
- filtros e validadores de seguranca;
- adaptador HTTP para a Procpag.

### 3.3 Arquitetura obrigatoria da solucao

O diagrama a seguir apresenta a visao arquitetural apenas com os servicos obrigatorios considerados nesta entrega.

```mermaid
flowchart LR
    cliente[Cliente]

    subgraph Plataforma["Connect Food - Fluxo obrigatorio"]
        auth[auth-service<br/>cadastro, login, refresh, JWT]
        order[order-service<br/>criar pedido, consultar por id,<br/>listar pedidos para admin/dono,<br/>consumir pagamento.aprovado]
        payment[payment-service<br/>consumir pedido.criado,<br/>integrar com Procpag,<br/>publicar eventos]
    end

    subgraph Infra["Infraestrutura"]
        postgres[(PostgreSQL<br/>schemas auth, order, payment)]
        kafka[(Kafka<br/>pedido.criado<br/>pagamento.aprovado<br/>pagamento.pendente)]
        procpag[processamento-pagamento-externo<br/>Procpag]
    end

    cliente -->|POST /auth/register<br/>POST /auth/login| auth
    cliente -->|GET /auth/me| auth
    cliente -->|POST /orders<br/>GET /orders/{uuid}| order
    cliente -->|GET /orders<br/>somente ADMIN/RESTAURANT_OWNER| order

    auth -->|persiste usuarios e refresh tokens| postgres
    order -->|persiste pedidos| postgres
    payment -->|persiste transacoes| postgres

    order -->|publica pedido.criado| kafka
    kafka -->|consome pedido.criado| payment
    payment -->|publica pagamento.aprovado| kafka
    payment -->|publica pagamento.pendente| kafka
    kafka -->|consome pagamento.aprovado| order

    payment -->|HTTP| procpag
```

### 3.4 Diagrama de componentes

```mermaid
flowchart LR
    cliente[Cliente]
    procpag[Procpag]
    kafka[(Kafka)]
    postgres[(PostgreSQL)]

    subgraph auth["auth-service"]
        authController[AuthController]
        registerUC[RegisterUserUseCase]
        loginUC[LoginUseCase]
        refreshUC[RefreshTokenUseCase]
        jwtIssuer[JwtIssuer / JwtValidator]
        userRepo[UserRepositoryAdapter]
        roleRepo[RoleRepositoryAdapter]
        refreshRepo[RefreshTokenRepositoryAdapter]
    end

    subgraph order["order-service"]
        orderController[OrderController]
        createOrderUC[CreateOrderUseCase]
        getOrderUC[GetOrderByUuidUseCase]
        listOrdersUC[ListOrdersByCustomerUseCase]
        updateStatusUC[UpdateOrderStatusUseCase]
        orderPublisher[KafkaOrderEventPublisher]
        paymentApprovedConsumer[PaymentStatusConsumer]
        orderRepo[OrderRepositoryAdapter]
        orderJwt[JwtAuthenticationFilter / JwtValidator]
    end

    subgraph payment["payment-service"]
        orderCreatedConsumer[OrderCreatedConsumer]
        pendingWorker[PaymentPendingWorker]
        processPaymentUC[ProcessPaymentOnOrderCreatedUseCase]
        reprocessPaymentUC[ReprocessPendingPaymentUseCase]
        orchestrator[PaymentOrchestratorService]
        resilience[PaymentResilienceExecutor]
        processor[ProcpagPaymentProcessorAdapter]
        paymentPublisher[KafkaPaymentEventPublisher]
        paymentRepo[PaymentTransactionRepositoryAdapter]
    end

    cliente --> authController
    cliente --> orderController

    authController --> registerUC
    authController --> loginUC
    authController --> refreshUC
    loginUC --> jwtIssuer
    refreshUC --> jwtIssuer
    registerUC --> roleRepo
    registerUC --> userRepo
    loginUC --> userRepo
    loginUC --> refreshRepo
    refreshUC --> refreshRepo
    auth --> postgres

    orderController --> orderJwt
    orderController --> createOrderUC
    orderController --> getOrderUC
    orderController --> listOrdersUC
    createOrderUC --> orderRepo
    getOrderUC --> orderRepo
    listOrdersUC --> orderRepo
    createOrderUC --> orderPublisher
    paymentApprovedConsumer --> updateStatusUC
    updateStatusUC --> orderRepo
    order --> postgres
    orderPublisher --> kafka
    kafka --> paymentApprovedConsumer

    kafka --> orderCreatedConsumer
    kafka --> pendingWorker
    orderCreatedConsumer --> processPaymentUC
    pendingWorker --> reprocessPaymentUC
    processPaymentUC --> paymentRepo
    reprocessPaymentUC --> paymentRepo
    processPaymentUC --> orchestrator
    reprocessPaymentUC --> orchestrator
    orchestrator --> resilience
    orchestrator --> processor
    orchestrator --> paymentPublisher
    orchestrator --> paymentRepo
    processor --> procpag
    payment --> postgres
    paymentPublisher --> kafka
```

### 3.5 Visao C4

```mermaid
C4Context
    title Contexto geral da solucao obrigatoria

    Person(cliente, "Cliente", "Usuario autenticado que cria e consulta pedidos")

    System_Boundary(connect_food, "Connect Food") {
        System(auth, "auth-service", "Cadastro, login, refresh token e emissao de JWT")
        System(order, "order-service", "Criacao, consulta e atualizacao de status do pedido")
        System(payment, "payment-service", "Processamento de pagamento, resiliencia e reprocessamento")
    }

    System_Ext(procpag, "Procpag", "Servico externo de processamento de pagamento")
    System_Ext(kafka, "Kafka", "Mensageria para eventos de pedido e pagamento")
    System_Ext(postgres, "PostgreSQL", "Persistencia dos schemas auth, order e payment")

    Rel(cliente, auth, "Cadastra, autentica e consulta identidade", "HTTP/JSON")
    Rel(cliente, order, "Cria e consulta pedidos", "HTTP/JSON com JWT")

    Rel(auth, postgres, "Persiste usuarios, roles e refresh tokens")
    Rel(order, postgres, "Persiste pedidos")
    Rel(payment, postgres, "Persiste transacoes de pagamento")

    Rel(order, kafka, "Publica pedido.criado")
    Rel(kafka, payment, "Entrega pedido.criado")
    Rel(payment, kafka, "Publica pagamento.aprovado e pagamento.pendente")
    Rel(kafka, order, "Entrega pagamento.aprovado")
    Rel(payment, procpag, "Solicita processamento do pagamento", "HTTP")
```

## 4. Tecnologias Utilizadas

O projeto foi desenvolvido em **Java 21**, utilizando **Spring Boot 4.0.2** como base para os servicos. Para persistencia foi adotado **PostgreSQL**, com **Spring Data JPA** como tecnologia ORM e **Flyway** para versionamento e criacao controlada do banco de dados.

No contexto de seguranca, foi utilizada a combinacao de **Spring Security** com **JWT**, por meio da biblioteca `jjwt`, permitindo autenticacao stateless e propagacao de identidade do usuario entre as requisicoes. Para documentacao das APIs REST, o projeto utiliza **Springdoc OpenAPI**.

Para comunicacao assicrona entre servicos foi adotado **Apache Kafka**, integrado via **Spring Kafka**. Ja para a integracao HTTP com o servico externo de pagamento, foi utilizado **Spring Cloud OpenFeign**. Os mecanismos de tratamento de falhas foram implementados com **Resilience4j**, contemplando os padroes exigidos pelo enunciado.

No aspecto operacional e de entrega, a solucao utiliza **Docker** e **Docker Compose** para orquestracao local dos servicos, enquanto o **Maven** foi adotado como ferramenta de build e gerenciamento de dependencias.

### 4.1 Resumo da stack

| Tecnologia | Uso no projeto |
| --- | --- |
| Java 21 | Linguagem principal |
| Spring Boot 4.0.2 | Base dos servicos |
| Spring Web MVC | Exposicao das APIs HTTP |
| Spring Security | Controle de acesso |
| JWT (`jjwt`) | Emissao e validacao de tokens |
| Spring Data JPA | Persistencia |
| PostgreSQL | Banco de dados |
| Flyway | Migracoes e versionamento do banco |
| Apache Kafka | Integracao por eventos |
| Spring Kafka | Producer e consumer |
| Spring Cloud OpenFeign | Cliente HTTP para Procpag |
| Resilience4j | Retry, Timeout e Circuit Breaker |
| Springdoc OpenAPI | Swagger/OpenAPI |
| Lombok | Reducao de boilerplate |
| Docker / Compose | Execucao local conteinerizada |
| Maven | Build e dependencias |

## 5. Modelagem de Dominio e Banco de Dados

O modelo de dominio foi definido a partir dos requisitos funcionais e nao funcionais presentes no `tc-3.md`. O sistema foi particionado em tres contextos principais: autenticacao, pedidos e pagamentos.

### 5.1 Contexto de autenticacao

O schema `auth` concentra a estrutura de identidade e acesso. As principais tabelas identificadas nas migracoes sao:

- `roles`: armazena os papeis de acesso;
- `users`: armazena os usuarios da plataforma;
- `user_roles`: representa a associacao entre usuarios e papeis;
- `refresh_tokens`: armazena o hash dos refresh tokens, sua validade e eventual revogacao.

Papeis semeados na base:

- `ADMIN`
- `CUSTOMER`
- `RESTAURANT_OWNER`

Do ponto de vista de dominio, o servico trabalha principalmente com:

- `User`
- `Role`

A principal responsabilidade desse contexto e autenticar o usuario, emitir tokens e preservar a rastreabilidade da sessao por meio do refresh token.

### 5.2 Contexto de pedidos

O schema `order` representa o agregado de pedido. As tabelas principais sao:

- `orders`
- `order_items`

A entidade `Order` encapsula:

- `uuid` do pedido;
- `customerUuid` do usuario autenticado;
- `restaurantId`;
- `status`;
- `totalAmount`;
- lista de itens.

Cada item do pedido contem:

- identificador do item;
- nome;
- quantidade;
- preco unitario.

O status do pedido e expresso pelo enum `OrderStatus`, com os seguintes valores:

- `CREATED`
- `PENDING_PAYMENT`
- `PAID`
- `CANCELLED`

### 5.3 Contexto de pagamentos

O schema `payment` possui como estrutura principal a tabela `payment_transaction`, responsavel por registrar a tentativa de pagamento associada a cada pedido.

Campos relevantes:

- `uuid`
- `order_uuid`
- `customer_uuid`
- `status`
- `amount`
- `pending_reprocess_attempts`

O status do pagamento e expresso pelo enum `PaymentStatus`, com os valores:

- `APPROVED`
- `PENDING`
- `FAILED`

Esse contexto concentra a resiliencia operacional da solucao, uma vez que lida com indisponibilidade de servico externo e reprocessamento automatico.

### 5.4 Consideracoes sobre a modelagem

Em todos os servicos, observa-se a existencia de separacao entre:

- modelo de dominio;
- entidades de persistencia;
- adaptadores de repositorio;
- mapeadores entre dominio e infraestrutura.

Essa separacao reforca a aderencia do projeto a uma abordagem arquitetural limpa, reduzindo o acoplamento entre o nucleo do dominio e os detalhes de banco de dados.

## 6. Casos de Uso e Fluxos Principais

Esta secao apresenta os principais casos de uso implementados e os respectivos fluxos de execucao. Alem da descricao textual, os diagramas foram incorporados diretamente no documento para renderizacao.

### 6.1 Cadastro, login e renovacao de sessao

No `auth-service`, os casos de uso `RegisterUserUseCase`, `LoginUseCase` e `RefreshTokenUseCase` sao responsaveis por:

- cadastrar usuarios com senha hasheada;
- validar credenciais;
- emitir access token e refresh token;
- revogar refresh token anterior;
- gerar novo par de tokens quando necessario.

Fluxo academico resumido:

1. o cliente envia a requisicao para cadastro ou autenticacao;
2. o controller converte o request em DTO de aplicacao;
3. o caso de uso executa as validacoes necessarias;
4. o adaptador de persistencia interage com o banco;
5. o servico retorna resposta padronizada ao consumidor.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Auth as auth-service
    participant AuthDB as PostgreSQL auth

    rect rgb(245, 248, 252)
        Note over Cliente,AuthDB: Cadastro de usuario
        Cliente->>Auth: POST /auth/register
        activate Auth
        Auth->>AuthDB: valida email e busca role
        activate AuthDB
        AuthDB-->>Auth: role e validacao de email
        deactivate AuthDB
        Auth->>AuthDB: salva usuario com senha hasheada
        activate AuthDB
        AuthDB-->>Auth: usuario persistido
        deactivate AuthDB
        Auth-->>Cliente: 201 Created
        deactivate Auth
    end

    rect rgb(250, 248, 242)
        Note over Cliente,AuthDB: Login
        Cliente->>Auth: POST /auth/login
        activate Auth
        Auth->>AuthDB: busca usuario por email
        activate AuthDB
        AuthDB-->>Auth: usuario e roles
        deactivate AuthDB
        Auth->>Auth: valida senha
        Auth->>Auth: gera access token JWT + refresh token
        Auth->>AuthDB: salva hash do refresh token
        activate AuthDB
        AuthDB-->>Auth: refresh token salvo
        deactivate AuthDB
        Auth-->>Cliente: accessToken + refreshToken
        deactivate Auth
    end

    rect rgb(242, 250, 244)
        Note over Cliente,AuthDB: Renovacao de token
        Cliente->>Auth: POST /auth/refresh
        activate Auth
        Auth->>AuthDB: valida refresh token vigente
        activate AuthDB
        AuthDB-->>Auth: token valido
        deactivate AuthDB
        Auth->>AuthDB: revoga token anterior
        activate AuthDB
        AuthDB-->>Auth: token revogado
        deactivate AuthDB
        Auth->>Auth: gera novo par de tokens
        Auth->>AuthDB: salva novo hash
        activate AuthDB
        AuthDB-->>Auth: novo hash salvo
        deactivate AuthDB
        Auth-->>Cliente: novo accessToken + refreshToken
        deactivate Auth
    end
```

### 6.2 Criacao e consulta de pedidos

O `order-service` implementa os seguintes casos de uso principais:

- `CreateOrderUseCase`
- `GetOrderByUuidUseCase`
- `ListOrdersByCustomerUseCase`
- `UpdateOrderStatusUseCase`

No fluxo de criacao, o sistema extrai o identificador do cliente a partir do JWT, valida a lista de itens, calcula o valor total, persiste o pedido e publica o evento `pedido.criado`. Nas consultas, o pedido pode ser recuperado individualmente por qualquer usuario autenticado, enquanto a listagem `GET /orders` fica restrita a usuarios com papel `ADMIN` ou `RESTAURANT_OWNER`.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    actor Gestor as Admin ou Dono do Restaurante
    participant Order as order-service
    participant OrderDB as PostgreSQL order
    participant Kafka as Kafka

    rect rgb(245, 248, 252)
        Note over Cliente,Kafka: Criacao do pedido
        Cliente->>Order: POST /orders com JWT e itens
        activate Order
        Order->>Order: extrai customerUuid do JWT
        Order->>Order: valida itens e calcula total
        Order->>OrderDB: salva pedido com status CREATED
        activate OrderDB
        OrderDB-->>Order: pedido persistido
        deactivate OrderDB
        Order->>Kafka: publica evento pedido.criado
        activate Kafka
        Kafka-->>Order: evento recebido no topico
        deactivate Kafka
        Order-->>Cliente: 201 Created com orderUuid e total
        deactivate Order
    end

    rect rgb(250, 248, 242)
        Note over Cliente,OrderDB: Consulta por id
        Cliente->>Order: GET /orders/{uuid}
        activate Order
        Order->>OrderDB: busca pedido por uuid
        activate OrderDB
        OrderDB-->>Order: pedido encontrado
        deactivate OrderDB
        Order-->>Cliente: dados do pedido
        deactivate Order
    end

    rect rgb(242, 250, 244)
        Note over Gestor,OrderDB: Listagem de pedidos disponivel apenas para ADMIN e RESTAURANT_OWNER
        Gestor->>Order: GET /orders com JWT
        activate Order
        Order->>Order: valida role ADMIN ou RESTAURANT_OWNER
        Order->>OrderDB: lista pedidos por usuario autenticado
        activate OrderDB
        OrderDB-->>Order: lista de pedidos
        deactivate OrderDB
        Order-->>Gestor: lista de pedidos
        deactivate Order
    end

    rect rgb(252, 244, 244)
        Note over Cliente,Order: Restricao para cliente autenticado
        Cliente->>Order: GET /orders com JWT de CUSTOMER
        activate Order
        Order->>Order: valida role
        Order-->>Cliente: 403 Forbidden
        deactivate Order
    end
```

### 6.3 Fluxo principal de pagamento aprovado

O caso de uso `ProcessPaymentOnOrderCreatedUseCase`, em conjunto com `PaymentOrchestratorService`, e responsavel por tratar o evento `pedido.criado`, persistir a transacao de pagamento, executar a chamada para a Procpag e, em caso de sucesso, publicar `pagamento.aprovado`.

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Order as order-service
    participant Kafka as Kafka
    participant Payment as payment-service
    participant PayDB as PostgreSQL payment
    participant Procpag as Procpag
    participant OrderDB as PostgreSQL order

    Cliente->>Order: POST /orders
    activate Order
    Order->>OrderDB: salva pedido com status CREATED
    activate OrderDB
    OrderDB-->>Order: pedido persistido
    deactivate OrderDB
    Order->>Kafka: publica pedido.criado
    activate Kafka
    Kafka-->>Order: evento publicado
    deactivate Kafka
    deactivate Order

    Kafka->>Payment: entrega pedido.criado
    activate Payment
    Payment->>PayDB: cria transacao PENDING se nao existir
    activate PayDB
    PayDB-->>Payment: transacao pronta
    deactivate PayDB
    Payment->>Payment: executa Retry + Timeout + Circuit Breaker
    Payment->>Procpag: requisicao HTTP de pagamento
    activate Procpag
    Procpag-->>Payment: approved / accepted
    deactivate Procpag
    Payment->>PayDB: atualiza transacao para APPROVED
    activate PayDB
    PayDB-->>Payment: transacao aprovada persistida
    deactivate PayDB
    Payment->>Kafka: publica pagamento.aprovado
    activate Kafka
    Kafka-->>Payment: evento publicado
    deactivate Kafka
    deactivate Payment

    Kafka->>Order: entrega pagamento.aprovado
    activate Order
    Order->>OrderDB: atualiza status do pedido para PAID
    activate OrderDB
    OrderDB-->>Order: pedido atualizado
    deactivate OrderDB
    Order-->>Cliente: pedido pode ser consultado como PAID
    deactivate Order
```

### 6.4 Fluxo de pagamento pendente

Quando a integracao com a Procpag falha, expira ou retorna um status nao aprovado, o sistema nao derruba o processamento como erro terminal. Em vez disso, registra a transacao como pendente e publica um evento proprio para reprocessamento.

```mermaid
sequenceDiagram
    autonumber
    participant Order as order-service
    participant Kafka as Kafka
    participant Payment as payment-service
    participant PayDB as PostgreSQL payment
    participant Res as Resilience4j
    participant Procpag as Procpag

    Order->>Kafka: publica pedido.criado
    activate Order
    activate Kafka
    Kafka-->>Order: evento publicado
    deactivate Kafka
    deactivate Order
    Kafka->>Payment: entrega pedido.criado
    activate Payment
    Payment->>PayDB: cria ou recupera transacao
    activate PayDB
    PayDB-->>Payment: transacao pendente
    deactivate PayDB
    Payment->>Res: aplica Retry + Timeout + Circuit Breaker
    Res->>Procpag: tenta processar pagamento

    alt Procpag indisponivel, timeout ou erro
        Procpag-->>Res: falha
        Res-->>Payment: excecao apos tentativas
        Payment->>PayDB: salva transacao como PENDING
        activate PayDB
        PayDB-->>Payment: transacao persistida
        deactivate PayDB
        Payment->>Kafka: publica pagamento.pendente
        activate Kafka
        Kafka-->>Payment: evento publicado
        deactivate Kafka
    else resposta nao aprovada
        Procpag-->>Res: status nao aprovado
        Res-->>Payment: retorno tratado como pendente
        Payment->>PayDB: salva transacao como PENDING
        activate PayDB
        PayDB-->>Payment: transacao persistida
        deactivate PayDB
        Payment->>Kafka: publica pagamento.pendente
        activate Kafka
        Kafka-->>Payment: evento publicado
        deactivate Kafka
    end
    deactivate Payment
```

### 6.5 Reprocessamento automatico de pagamento

O worker `PaymentPendingWorker` executa o reprocessamento de transacoes pendentes. Esse processo considera o estado do circuit breaker e controla o numero de tentativas por meio do campo `pending_reprocess_attempts`.

```mermaid
sequenceDiagram
    autonumber
    participant Kafka as Kafka
    participant Worker as PaymentPendingWorker
    participant PayDB as PostgreSQL payment
    participant Res as Resilience4j
    participant Payment as PaymentOrchestratorService
    participant Procpag as Procpag

    Kafka->>Worker: entrega pagamento.pendente
    activate Worker
    Worker->>PayDB: busca ou cria transacao pendente
    activate PayDB
    PayDB-->>Worker: transacao atual
    deactivate PayDB
    Worker->>Res: verifica estado do circuit breaker

    alt circuit breaker aberto
        Res-->>Worker: circuito aberto
        Worker->>PayDB: incrementa pendingReprocessAttempts
        activate PayDB
        PayDB-->>Worker: tentativa atualizada
        deactivate PayDB
        Worker->>Kafka: nack com delay
        activate Kafka
        Kafka-->>Worker: reentrega agendada
        deactivate Kafka
    else circuito fechado ou half-open
        Res-->>Worker: circuito permite processamento
        Worker->>Payment: executa reprocessamento sem republicar evento pendente
        activate Payment
        Payment->>Procpag: nova tentativa de cobranca

        alt pagamento aprovado
            Procpag-->>Payment: approved
            Payment->>PayDB: salva transacao como APPROVED
            activate PayDB
            PayDB-->>Payment: transacao aprovada persistida
            deactivate PayDB
            Payment->>Kafka: publica pagamento.aprovado
            activate Kafka
            Kafka-->>Payment: evento publicado
            deactivate Kafka
            Worker->>Kafka: ack
            activate Kafka
            Kafka-->>Worker: mensagem finalizada
            deactivate Kafka
        else ainda pendente
            Procpag-->>Payment: erro, timeout ou nao aprovado
            Payment->>PayDB: mantem status PENDING
            activate PayDB
            PayDB-->>Payment: transacao mantida
            deactivate PayDB
            Worker->>PayDB: incrementa pendingReprocessAttempts
            activate PayDB
            PayDB-->>Worker: tentativa atualizada
            deactivate PayDB
            Worker->>Kafka: nack com delay
            activate Kafka
            Kafka-->>Worker: reentrega agendada
            deactivate Kafka
        end
        deactivate Payment
    end
    deactivate Worker
```

## 7. Endpoints da API

Os endpoints implementados para os fluxos obrigatorios estao concentrados nos servicos `auth-service` e `order-service`. O `payment-service`, na implementacao atual, atua principalmente por eventos e integracao HTTP com a Procpag, nao expondo endpoints REST de negocio centrais para o fluxo principal.

### 7.1 Endpoints do auth-service

| Metodo | Endpoint | Finalidade | Requer JWT |
| --- | --- | --- | --- |
| POST | `/auth/register` | Cadastrar usuario | Nao |
| POST | `/auth/login` | Autenticar usuario e emitir tokens | Nao |
| POST | `/auth/refresh` | Renovar sessao com refresh token | Nao |
| GET | `/auth/me` | Retornar dados do usuario autenticado | Sim |

### 7.2 Endpoints do order-service

| Metodo | Endpoint | Finalidade | Requer JWT |
| --- | --- | --- | --- |
| POST | `/orders` | Criar pedido | Sim |
| GET | `/orders/{uuid}` | Consultar pedido por identificador | Sim |
| GET | `/orders` | Listar pedidos do usuario autenticado, restrito a `ADMIN` e `RESTAURANT_OWNER` | Sim |

### 7.3 Observacoes de documentacao OpenAPI

Os servicos HTTP utilizam Springdoc OpenAPI, permitindo a geracao de documentacao Swagger/OpenAPI. No `auth-service`, por exemplo, estao configurados os caminhos:

- `/v3/api-docs`
- `/swagger-ui`

Essa capacidade favorece demonstracao academica, exploracao manual da API e validacao funcional durante a apresentacao.

## 8. Padroes de Resposta e Tratamentos de Erros

### 8.1 Respostas de sucesso

As respostas de sucesso seguem o padrao esperado de uma API REST:

- `201 Created` para criacao de recursos, como cadastro de usuario e criacao de pedido;
- `200 OK` para autenticacao, renovacao de token e consultas.

Exemplos de respostas:

#### 8.1.1 Login

```json
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "expiresInSeconds": 900
}
```

#### 8.1.2 Consulta de autenticacao

```json
{
  "userUuid": "uuid-do-usuario",
  "roles": ["CUSTOMER"]
}
```

### 8.2 Tratamento de erros no auth-service

O `auth-service` utiliza um modelo proprio de erro inspirado em Problem Details. Os campos identificados no DTO sao:

- `type`
- `title`
- `status`
- `detail`
- `instance`
- `errors`

Status tratados pelo `GlobalExceptionHandler`:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `500 Internal Server Error`

Exemplo:

```json
{
  "type": "https://httpstatuses.com/400",
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid input data",
  "instance": "/auth/register",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    }
  ]
}
```

### 8.3 Tratamento de erros no order-service

O `order-service` utiliza `ProblemDetail`, com aderencia ao estilo RFC 7807, enriquecido com propriedades adicionais.

Campos identificados:

- `type`
- `title`
- `detail`
- `instance`
- `timestamp`
- `errors` para validacao

Status tratados:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `500 Internal Server Error`

Exemplo conceitual:

```json
{
  "type": "https://connectfood.com/problems/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/orders",
  "timestamp": "2026-03-17T10:00:00Z",
  "errors": [
    {
      "field": "items",
      "message": "must not be empty"
    }
  ]
}
```

## 9. Collection para Testes

O enunciado da fase solicita a disponibilizacao de uma collection de testes de endpoints em ferramentas como Postman, Bruno ou Insomnia. Durante a analise do repositório atual, nao foi identificado arquivo de collection versionado.

Do ponto de vista academico, esta secao e importante porque demonstra a capacidade de reproducao e validacao da entrega. Assim, recomenda-se que a versao final da submissao inclua:

- uma collection Postman ou Bruno;
- um ambiente configurado com as URLs locais dos servicos;
- scripts ou variaveis para JWT e identificadores;
- cenarios de teste cobrindo:
  - cadastro de usuario;
  - login;
  - refresh token;
  - criacao de pedido;
  - consulta de pedido por UUID;
  - listagem de pedidos para `ADMIN` e `RESTAURANT_OWNER`;
  - validacao de bloqueio do `CUSTOMER` na rota `GET /orders`.

Caso a equipe deseje, essa collection pode ser adicionada em iteracao posterior ao repositorio e referenciada diretamente nesta secao.

## 10. Repositorio do Codigo

O repositório Git configurado no workspace analisado aponta para o seguinte remoto:

`git@github.com:connectFood/adjt-fase3-connect-food.git`

O `README.md` do projeto indica que a equipe deve trabalhar a partir de um fork desse repositorio base. Portanto, para a entrega final, recomenda-se substituir essa referencia pelo link efetivamente pertencente ao repositorio da equipe, caso exista um repositório derivado.

Em termos de conteudo, o repositorio contem:

- codigo-fonte dos servicos;
- `compose.yml` para subida da infraestrutura;
- arquivos Dockerfile por servico;
- scripts de migracao Flyway;
- documentacao de apoio em `docs`;
- diagramas Mermaid elaborados para esta fase.

## 11. Consideracoes Finais

Com base na especificacao de `docs/tc-3.md` e na implementacao existente, conclui-se que a solucao proposta apresenta uma estrutura coerente com os objetivos da Fase 3 do Tech Challenge. O projeto demonstra:

- separacao clara de responsabilidades em servicos independentes;
- uso adequado de autenticacao baseada em JWT;
- implementacao de criacao e consulta de pedidos;
- integracao assicrona entre pedido e pagamento;
- uso de mecanismos de resiliencia frente a falhas externas;
- reprocessamento de pagamentos pendentes de forma automatizada.

Do ponto de vista academico, a entrega evidencia dominio de conceitos relevantes de engenharia de software moderna, tais como arquitetura limpa, mensageria orientada a eventos, seguranca em APIs REST e tolerancia a falhas em sistemas distribuidos.

Como oportunidades de consolidacao da entrega, destacam-se:

- adicionar a collection de testes requisitada no enunciado;
- complementar a documentacao com dados formais da equipe;
- revisar o roteiro de apresentacao para demonstrar os fluxos principais em execucao;
- avaliar, em futura evolucao, a extensao do tratamento de `PENDENTE_PAGAMENTO` tambem no contexto de pedido, caso seja desejado maior alinhamento semantico entre pedido e pagamento.

Em sintese, o Connect Food, na forma implementada para esta fase, constitui uma base tecnica consistente, escalavel e aderente aos requisitos centrais do desafio proposto.

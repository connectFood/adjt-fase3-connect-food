# Fluxo de criacao e consulta de pedidos

Este arquivo cobre os fluxos obrigatorios implementados no `order-service`: criacao do pedido, consulta por identificador e listagem restrita a `ADMIN` e `RESTAURANT_OWNER`.

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

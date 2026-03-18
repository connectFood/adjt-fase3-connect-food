# Fluxo de pagamento pendente

Este diagrama mostra o comportamento de resiliencia implementado quando a integracao com a Procpag falha, expira ou retorna status nao aprovado.

Observacao: no codigo atual, o evento `pagamento.pendente` e tratado no `payment-service` para reprocessamento. O `order-service` implementado consome apenas `pagamento.aprovado`.

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

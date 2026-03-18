# Fluxo de reprocessamento de pagamento

O `payment-service` possui um worker dedicado para reprocessar mensagens `pagamento.pendente` ate aprovacao ou ate atingir o limite de tentativas configurado.

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

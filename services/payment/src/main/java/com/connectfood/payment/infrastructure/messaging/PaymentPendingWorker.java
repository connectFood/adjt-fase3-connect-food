package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.application.command.PendingPaymentCommand;
import com.connectfood.payment.application.service.PaymentResilienceExecutor;
import com.connectfood.payment.application.usecase.ReprocessPendingPaymentUseCase;
import com.connectfood.payment.domain.event.PaymentPendingEvent;
import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.domain.port.PaymentTransactionRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentPendingWorker {

  private final ObjectMapper mapper;
  private final ReprocessPendingPaymentUseCase useCase;
  private final PaymentTransactionRepositoryPort repository;
  private final PaymentResilienceExecutor resilienceExecutor;
  private final int maxReprocessAttempts;
  private final long reprocessDelayMs;

  public PaymentPendingWorker(
      ObjectMapper mapper,
      ReprocessPendingPaymentUseCase useCase,
      PaymentTransactionRepositoryPort repository,
      PaymentResilienceExecutor resilienceExecutor,
      @Value("${payment.pending-reprocess.max-attempts:8}") int maxReprocessAttempts,
      @Value("${payment.pending-reprocess.delay-ms:5000}") long reprocessDelayMs
  ) {
    this.mapper = mapper;
    this.useCase = useCase;
    this.repository = repository;
    this.resilienceExecutor = resilienceExecutor;
    this.maxReprocessAttempts = maxReprocessAttempts;
    this.reprocessDelayMs = reprocessDelayMs;
  }

  @KafkaListener(topics = "${payment.topics.payment-pending:pagamento.pendente}", groupId = "payment-worker")
  public void onPending(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    var payload = record.value();
    try {
      var event = mapper.readValue(payload, PaymentPendingEvent.class);
      var transaction = getOrCreateTransaction(event);
      var attempt = transaction.pendingReprocessAttempts();

      log.info("I=Evento pagamento.pendente recebido para reprocessamento. tentativa={}", attempt);
      if (attempt >= maxReprocessAttempts) {
        log.error(
            "E=Limite maximo de tentativas atingido para pedido {}. Mensagem finalizada sem novo reenvio",
            event.orderUuid()
        );
        acknowledgment.acknowledge();
        return;
      }

      if (resilienceExecutor.isCircuitOpen()) {
        log.warn(
            "W=Circuit breaker aberto. Mantendo pedido {} na fila para nova tentativa. tentativa={}",
            event.orderUuid(),
            attempt
        );
        repository.save(transaction.incrementPendingReprocessAttempts());
        acknowledgment.nack(Duration.ofMillis(reprocessDelayMs));
        return;
      }

      log.info("I=Iniciando reprocessamento do pagamento do pedido {}", event.orderUuid());
      var status = useCase.execute(new PendingPaymentCommand(
          event.paymentUuid(),
          event.orderUuid(),
          event.customerUuid(),
          event.amount(),
          event.reason()
      ));
      if (status != PaymentStatus.APPROVED) {
        log.warn(
            "W=Pagamento do pedido {} continua pendente. Mantendo mensagem na fila para nova tentativa. tentativa={}",
            event.orderUuid(),
            attempt
        );
        repository.save(transaction.incrementPendingReprocessAttempts());
        acknowledgment.nack(Duration.ofMillis(reprocessDelayMs));
        return;
      }
      log.info("I=Reprocessamento de pagamento finalizado para pedido {}", event.orderUuid());
      acknowledgment.acknowledge();
    } catch (Exception ex) {
      log.error(
          "E=Falha ao processar evento pagamento.pendente mensagem={} payload={}",
          ex.getMessage(),
          payload
      );
      acknowledgment.acknowledge();
    }
  }

  private PaymentTransaction getOrCreateTransaction(PaymentPendingEvent event) {
    return repository.findByOrderUuid(event.orderUuid())
        .orElseGet(() -> repository.save(PaymentTransaction.newPending(
            event.paymentUuid(),
            event.orderUuid(),
            event.customerUuid(),
            event.amount()
        )));
  }
}

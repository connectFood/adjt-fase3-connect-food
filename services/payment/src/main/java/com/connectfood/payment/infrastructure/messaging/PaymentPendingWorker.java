package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.application.command.PendingPaymentCommand;
import com.connectfood.payment.application.usecase.ReprocessPendingPaymentUseCase;
import com.connectfood.payment.domain.event.PaymentPendingEvent;
import com.connectfood.payment.domain.model.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentPendingWorker {

  private final ObjectMapper mapper;
  private final ReprocessPendingPaymentUseCase useCase;

  public PaymentPendingWorker(ObjectMapper mapper, ReprocessPendingPaymentUseCase useCase) {
    this.mapper = mapper;
    this.useCase = useCase;
  }

  @RetryableTopic(
      attempts = "${payment.pending-retry.attempts:4}",
      autoCreateTopics = "true",
      dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR,
      backOff = @BackOff(
          delayString = "${payment.pending-retry.delay-ms:10000}",
          multiplierString = "${payment.pending-retry.multiplier:2.0}",
          maxDelayString = "${payment.pending-retry.max-delay-ms:120000}"
      ),
      retryTopicSuffix = "-retry",
      dltTopicSuffix = "-dlt"
  )
  @KafkaListener(topics = "${payment.topics.payment-pending:pagamento.pendente}", groupId = "payment-worker")
  public void onPending(String payload) {
    try {
      log.info("I=Evento pagamento.pendente recebido para reprocessamento");
      var event = mapper.readValue(payload, PaymentPendingEvent.class);
      log.info("I=Iniciando reprocessamento do pagamento do pedido {}", event.orderUuid());
      var status = useCase.execute(new PendingPaymentCommand(
          event.paymentUuid(),
          event.orderUuid(),
          event.customerUuid(),
          event.amount(),
          event.reason()
      ));
      if (status != PaymentStatus.APPROVED) {
        log.warn("W=Pagamento do pedido {} continua pendente. Mensagem sera reenfileirada", event.orderUuid());
        throw new IllegalStateException("Pagamento ainda pendente para pedido " + event.orderUuid());
      }
      log.info("I=Reprocessamento de pagamento finalizado para pedido {}", event.orderUuid());
    } catch (Exception ex) {
      log.error("E=Falha ao processar evento pagamento.pendente payload={}", payload, ex);
      throw new IllegalStateException("Falha no processamento de pagamento pendente", ex);
    }
  }

  @DltHandler
  public void onDlt(String payload) {
    log.error("E=Mensagem enviada para DLT apos esgotar tentativas. Payload={}", payload);
  }
}

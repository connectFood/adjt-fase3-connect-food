package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.application.command.PendingPaymentCommand;
import com.connectfood.payment.application.usecase.ReprocessPendingPaymentUseCase;
import com.connectfood.payment.domain.event.PaymentPendingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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

  @KafkaListener(topics = "${payment.topics.payment-pending:pagamento.pendente}", groupId = "payment-worker")
  public void onPending(String payload) {
    try {
      log.info("I=Evento pagamento.pendente recebido para reprocessamento");
      var event = mapper.readValue(payload, PaymentPendingEvent.class);
      log.info("I=Iniciando reprocessamento do pagamento do pedido {}", event.orderUuid());
      useCase.execute(new PendingPaymentCommand(
          event.paymentUuid(),
          event.orderUuid(),
          event.customerUuid(),
          event.amount(),
          event.reason()
      ));
      log.info("I=Reprocessamento de pagamento finalizado para pedido {}", event.orderUuid());
    } catch (Exception ex) {
      log.error("E=Falha ao processar evento pagamento.pendente payload={}", payload, ex);
    }
  }
}

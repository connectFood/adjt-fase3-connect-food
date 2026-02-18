package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.infrastructure.messaging.events.PaymentPendingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentPendingWorker {

  private final ObjectMapper mapper;

  public PaymentPendingWorker(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @KafkaListener(topics = "${payment.topics.payment-pending:pagamento.pendente}", groupId = "payment-worker")
  public void onPending(String payload) {
    try {
      var event = mapper.readValue(payload, PaymentPendingEvent.class);

      // Próximo passo:
      // - tentar chamar procpag novamente
      // - se OK -> publicar pagamento.aprovado
      // - se falhar -> manter pendente / backoff
    } catch (Exception ignored) {
    }
  }
}

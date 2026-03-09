package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.domain.event.PaymentApprovedEvent;
import com.connectfood.payment.domain.event.PaymentPendingEvent;
import com.connectfood.payment.domain.port.PaymentEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisherPort {

  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper mapper;
  private final String approvedTopic;
  private final String pendingTopic;

  public KafkaPaymentEventPublisher(
      KafkaTemplate<String, String> kafka,
      ObjectMapper mapper,
      @Value("${payment.topics.payment-approved}") String approvedTopic,
      @Value("${payment.topics.payment-pending}") String pendingTopic
  ) {
    this.kafka = kafka;
    this.mapper = mapper;
    this.approvedTopic = approvedTopic;
    this.pendingTopic = pendingTopic;
  }

  @Override
  public void publishApproved(PaymentApprovedEvent event) {
    try {
      kafka.send(approvedTopic, event.orderUuid().toString(), mapper.writeValueAsString(event));
      log.info("I=Evento pagamento.aprovado enviado para pedido {}", event.orderUuid());
    } catch (Exception ex) {
      log.error("E=Falha ao enviar evento pagamento.aprovado para pedido {}", event.orderUuid(), ex);
    }
  }

  @Override
  public void publishPending(PaymentPendingEvent event) {
    try {
      kafka.send(pendingTopic, event.orderUuid().toString(), mapper.writeValueAsString(event));
      log.info("I=Evento pagamento.pendente enviado para pedido {} com motivo {}", event.orderUuid(), event.reason());
    } catch (Exception ex) {
      log.error("E=Falha ao enviar evento pagamento.pendente para pedido {}", event.orderUuid(), ex);
    }
  }
}

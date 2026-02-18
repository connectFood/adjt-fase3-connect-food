package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.domain.port.PaymentEventPublisherPort;
import com.connectfood.payment.infrastructure.messaging.events.PaymentApprovedEvent;
import com.connectfood.payment.infrastructure.messaging.events.PaymentPendingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
      kafka.send(approvedTopic, event.orderUuid()
          .toString(), mapper.writeValueAsString(event)
      );
    } catch (Exception ignored) {
    }
  }

  @Override
  public void publishPending(PaymentPendingEvent event) {
    try {
      kafka.send(pendingTopic, event.orderUuid()
          .toString(), mapper.writeValueAsString(event)
      );
    } catch (Exception ignored) {
    }
  }
}

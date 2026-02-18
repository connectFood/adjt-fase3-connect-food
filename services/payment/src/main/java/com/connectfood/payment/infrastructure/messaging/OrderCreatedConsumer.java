package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.application.usecase.ProcessPaymentOnOrderCreatedUseCase;
import com.connectfood.payment.infrastructure.messaging.events.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

  private final ObjectMapper mapper;
  private final ProcessPaymentOnOrderCreatedUseCase useCase;

  public OrderCreatedConsumer(ObjectMapper mapper, ProcessPaymentOnOrderCreatedUseCase useCase) {
    this.mapper = mapper;
    this.useCase = useCase;
  }

  @KafkaListener(topics = "${payment.topics.order-created:pedido.criado}", groupId = "payment-service")
  public void onMessage(String payload) {
    try {
      var event = mapper.readValue(payload, OrderCreatedEvent.class);
      useCase.execute(event);
    } catch (Exception ignored) {
    }
  }
}

package com.connectfood.order.infrastructure.messaging;

import com.connectfood.order.application.usecase.UpdateOrderStatusUseCase;
import com.connectfood.order.domain.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusConsumer {

  private final ObjectMapper mapper;
  private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

  public PaymentStatusConsumer(ObjectMapper mapper, UpdateOrderStatusUseCase updateOrderStatusUseCase) {
    this.mapper = mapper;
    this.updateOrderStatusUseCase = updateOrderStatusUseCase;
  }

  @KafkaListener(topics = "${order.topics.payment-approved:pagamento.aprovado}", groupId = "${spring.kafka.consumer.group-id:order-service}")
  public void onPaymentApproved(String payload) {
    try {
      var event = mapper.readValue(payload, PaymentApprovedEvent.class);
      updateOrderStatusUseCase.execute(event.orderUuid(), OrderStatus.PAID);
    } catch (Exception ignored) {
    }
  }
}

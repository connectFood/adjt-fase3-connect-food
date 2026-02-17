package com.connectfood.order.infrastructure.messaging;

import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.port.OrderEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisherPort {

  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper objectMapper;
  private final String orderCreatedTopic;

  public KafkaOrderEventPublisher(
      KafkaTemplate<String, String> kafka,
      ObjectMapper objectMapper,
      @Value("${order.topics.order-created}") String orderCreatedTopic
  ) {
    this.kafka = kafka;
    this.objectMapper = objectMapper;
    this.orderCreatedTopic = orderCreatedTopic;
  }

  @Override
  public void publishOrderCreated(Order order) {
    try {

      var event = new OrderCreatedEvent(
          order.uuid(),
          order.customerUuid(),
          order.restaurantId(),
          order.totalAmount(),
          order.items()
              .stream()
              .map(i -> new OrderCreatedEvent.Item(
                  i.itemId(),
                  i.itemName(),
                  i.quantity(),
                  i.unitPrice()
              ))
              .toList()
      );

      var json = objectMapper.writeValueAsString(event);

      // key = orderUuid → garante ordenação por pedido
      kafka.send(orderCreatedTopic, order.uuid()
          .toString(), json
      );

    } catch (Exception e) {
      // Para TC-3: não derrubar serviço por falha de publish
      // Ideal: logar o erro
    }
  }
}

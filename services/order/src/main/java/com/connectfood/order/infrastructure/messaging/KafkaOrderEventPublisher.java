package com.connectfood.order.infrastructure.messaging;

import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.port.OrderEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
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
    log.info("I=Iniciando publicação de evento de pedido criado orderUuid={} topico={}", order.uuid(), orderCreatedTopic);
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

      // key = orderUuid para garantir ordenacao por pedido
      kafka.send(orderCreatedTopic, order.uuid()
          .toString(), json
      );
      log.info("I=Evento de pedido criado publicado com sucesso orderUuid={} topico={}", order.uuid(), orderCreatedTopic);
    } catch (Exception e) {
      // Para TC-3: nao derrubar servico por falha de publish
      log.error("E=Erro ao publicar evento de pedido criado orderUuid={} topico={}", order.uuid(), orderCreatedTopic, e);
    }
  }
}

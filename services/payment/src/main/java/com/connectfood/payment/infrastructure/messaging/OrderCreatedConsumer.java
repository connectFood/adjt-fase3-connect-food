package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.application.command.OrderPaymentCommand;
import com.connectfood.payment.application.usecase.ProcessPaymentOnOrderCreatedUseCase;
import com.connectfood.payment.infrastructure.messaging.events.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
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
      log.info("I=Evento pedido.criado recebido para processamento");
      var event = mapper.readValue(payload, OrderCreatedEvent.class);
      log.info("I=Iniciando processamento de pagamento para pedido {}", event.orderUuid());
      useCase.execute(new OrderPaymentCommand(event.orderUuid(), event.customerUuid(), event.totalAmount()));
      log.info("I=Processamento de pedido.criado finalizado para pedido {}", event.orderUuid());
    } catch (Exception ex) {
      log.error("E=Falha ao processar evento pedido.criado payload={}", payload, ex);
    }
  }
}

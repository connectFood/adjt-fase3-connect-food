package com.connectfood.order.infrastructure.messaging;

import com.connectfood.order.application.usecase.UpdateOrderStatusUseCase;
import com.connectfood.order.domain.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
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
    log.info("I=Mensagem de pagamento aprovado recebida");
    try {
      var event = mapper.readValue(payload, PaymentApprovedEvent.class);
      var updated = updateOrderStatusUseCase.execute(event.orderUuid(), OrderStatus.PAID);
      if (updated) {
        log.info("I=Processamento de pagamento aprovado concluído orderUuid={} status={}", event.orderUuid(),
            OrderStatus.PAID
        );
      } else {
        log.warn("W=Pagamento aprovado recebido, mas pedido não encontrado orderUuid={}", event.orderUuid());
      }
    } catch (Exception e) {
      log.error("E=Erro ao processar mensagem de pagamento aprovado", e);
    }
  }
}

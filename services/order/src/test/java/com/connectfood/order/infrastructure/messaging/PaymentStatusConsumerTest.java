package com.connectfood.order.infrastructure.messaging;

import java.util.UUID;

import com.connectfood.order.application.usecase.UpdateOrderStatusUseCase;
import com.connectfood.order.domain.model.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class PaymentStatusConsumerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void shouldSetPaidWhenPaymentApprovedEventArrives() throws Exception {
    var useCase = Mockito.mock(UpdateOrderStatusUseCase.class);
    var consumer = new PaymentStatusConsumer(mapper, useCase);

    var orderUuid = UUID.randomUUID();
    var payload = mapper.writeValueAsString(
        new PaymentApprovedEvent(UUID.randomUUID(), orderUuid, UUID.randomUUID(), java.math.BigDecimal.TEN)
    );

    consumer.onPaymentApproved(payload);

    Mockito.verify(useCase)
        .execute(orderUuid, OrderStatus.PAID);
  }

  @Test
  void shouldIgnoreInvalidPayload() {
    var useCase = Mockito.mock(UpdateOrderStatusUseCase.class);
    var consumer = new PaymentStatusConsumer(mapper, useCase);

    consumer.onPaymentApproved("invalid-json");

    Mockito.verify(useCase, Mockito.never())
        .execute(ArgumentMatchers.any(), ArgumentMatchers.any());
  }
}

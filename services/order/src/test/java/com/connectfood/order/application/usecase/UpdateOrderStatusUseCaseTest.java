package com.connectfood.order.application.usecase;

import java.util.UUID;

import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UpdateOrderStatusUseCaseTest {

  @Test
  void shouldUpdateStatus() {
    var repository = Mockito.mock(OrderRepositoryPort.class);
    var orderUuid = UUID.randomUUID();
    Mockito.when(repository.updateStatusByUuid(orderUuid, OrderStatus.PAID))
        .thenReturn(true);

    var useCase = new UpdateOrderStatusUseCase(repository);
    var updated = useCase.execute(orderUuid, OrderStatus.PAID);

    Assertions.assertTrue(updated);
    Mockito.verify(repository)
        .updateStatusByUuid(orderUuid, OrderStatus.PAID);
  }
}

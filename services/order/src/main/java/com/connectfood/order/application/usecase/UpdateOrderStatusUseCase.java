package com.connectfood.order.application.usecase;

import java.util.UUID;

import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class UpdateOrderStatusUseCase {

  private final OrderRepositoryPort repository;

  public UpdateOrderStatusUseCase(OrderRepositoryPort repository) {
    this.repository = repository;
  }

  public boolean execute(UUID orderUuid, OrderStatus status) {
    return repository.updateStatusByUuid(orderUuid, status);
  }
}

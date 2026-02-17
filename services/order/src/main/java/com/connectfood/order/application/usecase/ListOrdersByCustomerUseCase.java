package com.connectfood.order.application.usecase;

import com.connectfood.order.application.dto.OrderOutput;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ListOrdersByCustomerUseCase {

  private final OrderRepositoryPort repository;

  public ListOrdersByCustomerUseCase(OrderRepositoryPort repository) {
    this.repository = repository;
  }

  public List<OrderOutput> execute(UUID customerUuid) {
    return repository.findByCustomerUuid(customerUuid)
        .stream()
        .map(o -> new OrderOutput(
            o.uuid(),
            o.customerUuid(),
            o.restaurantId(),
            o.status(),
            o.totalAmount(),
            o.items()
                .stream()
                .map(i -> new OrderOutput.OrderItemOutput(i.itemId(), i.itemName(), i.quantity(), i.unitPrice()))
                .toList()
        ))
        .toList();
  }
}

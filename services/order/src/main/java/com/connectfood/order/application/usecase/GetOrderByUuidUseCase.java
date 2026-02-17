package com.connectfood.order.application.usecase;

import java.util.UUID;

import com.connectfood.order.application.dto.OrderOutput;
import com.connectfood.order.domain.exception.NotFoundException;
import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class GetOrderByUuidUseCase {

  private final OrderRepositoryPort repository;

  public GetOrderByUuidUseCase(OrderRepositoryPort repository) {
    this.repository = repository;
  }

  public OrderOutput execute(UUID orderUuid) {
    var order = repository.findByUuid(orderUuid)
        .orElseThrow(() -> new NotFoundException("Order not found"));

    return toOutput(order);
  }

  private OrderOutput toOutput(Order order) {
    var items = order.items()
        .stream()
        .map(i -> new OrderOutput.OrderItemOutput(i.itemId(), i.itemName(), i.quantity(), i.unitPrice()))
        .toList();

    return new OrderOutput(
        order.uuid(),
        order.customerUuid(),
        order.restaurantId(),
        order.status(),
        order.totalAmount(),
        items
    );
  }
}

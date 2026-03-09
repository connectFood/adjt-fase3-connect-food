package com.connectfood.order.application.usecase;

import java.util.UUID;

import com.connectfood.order.application.dto.OrderOutput;
import com.connectfood.order.domain.exception.NotFoundException;
import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GetOrderByUuidUseCase {

  private final OrderRepositoryPort repository;

  public GetOrderByUuidUseCase(OrderRepositoryPort repository) {
    this.repository = repository;
  }

  public OrderOutput execute(UUID orderUuid) {
    log.info("I=Consultando pedido por uuid={}", orderUuid);
    var order = repository.findByUuid(orderUuid)
        .orElseThrow(() -> {
          log.warn("W=Pedido não encontrado para uuid={}", orderUuid);
          return new NotFoundException("Order not found");
        });

    log.info("I=Pedido encontrado com sucesso uuid={} status={}", order.uuid(), order.status());
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

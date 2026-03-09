package com.connectfood.order.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.connectfood.order.application.dto.CreateOrderInput;
import com.connectfood.order.application.dto.OrderOutput;
import com.connectfood.order.domain.exception.BadRequestException;
import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.model.OrderItem;
import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.domain.port.OrderEventPublisherPort;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreateOrderUseCase {

  private final OrderRepositoryPort orderRepository;
  private final OrderEventPublisherPort publisher;

  public CreateOrderUseCase(OrderRepositoryPort orderRepository, OrderEventPublisherPort publisher) {
    this.orderRepository = orderRepository;
    this.publisher = publisher;
  }

  public OrderOutput execute(UUID customerUuid, CreateOrderInput input) {
    log.info("I=Iniciando criação de pedido para customerUuid={}", customerUuid);
    if (input.items() == null || input.items()
        .isEmpty()) {
      log.warn("W=Falha na criação do pedido: lista de itens vazia para customerUuid={}", customerUuid);
      throw new BadRequestException("Order must have at least one item");
    }

    List<OrderItem> items = input.items()
        .stream()
        .map(i -> new OrderItem(i.itemId(), i.itemName(), i.quantity(), i.unitPrice()))
        .collect(Collectors.toList());

    var total = items.stream()
        .map(OrderItem::subtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    var order = new Order(
        null,
        null,
        customerUuid,
        input.restaurantId(),
        OrderStatus.CREATED,
        total,
        items
    );

    var saved = orderRepository.save(order);
    log.info("I=Pedido criado com sucesso uuid={} customerUuid={} total={}", saved.uuid(), saved.customerUuid(),
        saved.totalAmount()
    );

    // event
    publisher.publishOrderCreated(saved);
    log.info("I=Processo de publicação de evento de pedido criado acionado para orderUuid={}", saved.uuid());

    return toOutput(saved);
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

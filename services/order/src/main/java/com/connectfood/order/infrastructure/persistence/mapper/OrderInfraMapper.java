package com.connectfood.order.infrastructure.persistence.mapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.model.OrderItem;
import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.infrastructure.persistence.entity.OrderEntity;
import com.connectfood.order.infrastructure.persistence.entity.OrderItemEntity;

public final class OrderInfraMapper {
  private OrderInfraMapper() {
  }

  public static Order toDomain(OrderEntity e) {
    var items = e.getItems()
        .stream()
        .map(i -> new OrderItem(i.getItemId(), i.getItemName(), i.getQuantity(), i.getUnitPrice()))
        .toList();

    return new Order(
        e.getId(),
        e.getUuid(),
        e.getCustomerUuid(),
        e.getRestaurantId(),
        OrderStatus.valueOf(e.getStatus()),
        e.getTotalAmount(),
        items
    );
  }

  public static OrderEntity toEntity(Order order) {
    var now = OffsetDateTime.now();

    var e = new OrderEntity();
    e.setId(order.id());
    e.setUuid(order.uuid() == null ? UUID.randomUUID() : order.uuid());
    e.setCustomerUuid(order.customerUuid());
    e.setRestaurantId(order.restaurantId());
    e.setStatus(order.status()
        .name());
    e.setTotalAmount(order.totalAmount());

    e.setCreatedAt(now);
    e.setUpdatedAt(now);

    var itemEntities = order.items()
        .stream()
        .map(i -> {
          var ie = new OrderItemEntity();
          ie.setOrder(e);
          ie.setItemId(i.itemId());
          ie.setItemName(i.itemName());
          ie.setQuantity(i.quantity());
          ie.setUnitPrice(i.unitPrice());
          ie.setCreatedAt(now);
          return ie;
        })
        .toList();

    e.getItems()
        .clear();
    e.getItems()
        .addAll(itemEntities);

    return e;
  }
}

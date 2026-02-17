package com.connectfood.order.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Order(
    Long id,
    UUID uuid,
    UUID customerUuid,
    String restaurantId,
    OrderStatus status,
    BigDecimal totalAmount,
    List<OrderItem> items
) {
}

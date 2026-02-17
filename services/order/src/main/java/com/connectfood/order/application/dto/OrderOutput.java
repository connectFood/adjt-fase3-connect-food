package com.connectfood.order.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.connectfood.order.domain.model.OrderStatus;

public record OrderOutput(
    UUID orderUuid,
    UUID customerUuid,
    String restaurantId,
    OrderStatus status,
    BigDecimal totalAmount,
    List<OrderItemOutput> items
) {
  public record OrderItemOutput(
      String itemId,
      String itemName,
      int quantity,
      BigDecimal unitPrice
  ) {
  }
}

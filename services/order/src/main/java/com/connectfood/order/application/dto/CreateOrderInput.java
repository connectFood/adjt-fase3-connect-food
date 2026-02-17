package com.connectfood.order.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderInput(
    String restaurantId,
    List<CreateOrderItemInput> items
) {
  public record CreateOrderItemInput(
      String itemId,
      String itemName,
      int quantity,
      BigDecimal unitPrice
  ) {
  }
}

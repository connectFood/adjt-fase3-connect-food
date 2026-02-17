package com.connectfood.order.domain.model;

import java.math.BigDecimal;

public record OrderItem(
    String itemId,
    String itemName,
    int quantity,
    BigDecimal unitPrice
) {
  public BigDecimal subtotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}

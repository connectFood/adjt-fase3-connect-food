package com.connectfood.payment.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderUuid,
    UUID customerUuid,
    String restaurantId,
    BigDecimal totalAmount,
    List<Item> items
) {
  public record Item(String itemId, String itemName, int quantity, BigDecimal unitPrice) {
  }
}

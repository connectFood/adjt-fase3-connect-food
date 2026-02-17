package com.connectfood.order.entrypoint.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank String restaurantId,
    @NotNull List<Item> items
) {
  public record Item(
      @NotBlank String itemId,
      @NotBlank String itemName,
      @Positive int quantity,
      @NotNull BigDecimal unitPrice
  ) {
  }
}

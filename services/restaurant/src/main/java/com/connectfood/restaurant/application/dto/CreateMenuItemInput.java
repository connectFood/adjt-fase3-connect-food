package com.connectfood.restaurant.application.dto;

import java.math.BigDecimal;

public record CreateMenuItemInput(
    String itemCode,
    String name,
    String description,
    BigDecimal price,
    boolean available
) {
}

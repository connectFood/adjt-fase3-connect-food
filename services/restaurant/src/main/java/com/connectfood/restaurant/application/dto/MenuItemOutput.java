package com.connectfood.restaurant.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemOutput(
    UUID uuid,
    UUID restaurantUuid,
    String itemCode,
    String name,
    String description,
    BigDecimal price,
    boolean available
) {
}

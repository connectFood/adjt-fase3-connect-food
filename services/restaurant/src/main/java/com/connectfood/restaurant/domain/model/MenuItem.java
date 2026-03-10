package com.connectfood.restaurant.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItem(
    Long id,
    UUID uuid,
    UUID restaurantUuid,
    String itemCode,
    String name,
    String description,
    BigDecimal price,
    boolean available
) {
}

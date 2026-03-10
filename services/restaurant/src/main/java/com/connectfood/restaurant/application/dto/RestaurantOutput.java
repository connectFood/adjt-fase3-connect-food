package com.connectfood.restaurant.application.dto;

import java.util.UUID;

public record RestaurantOutput(
    UUID uuid,
    String name,
    String description,
    boolean active
) {
}

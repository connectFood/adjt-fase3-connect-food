package com.connectfood.restaurant.domain.model;

import java.util.UUID;

public record Restaurant(
    Long id,
    UUID uuid,
    String name,
    String description,
    boolean active
) {
}

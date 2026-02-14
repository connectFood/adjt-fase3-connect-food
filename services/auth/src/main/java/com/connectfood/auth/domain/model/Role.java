package com.connectfood.auth.domain.model;

import java.util.UUID;

public record Role(
    Long id,
    UUID uuid,
    String name,
    String description
) {
}

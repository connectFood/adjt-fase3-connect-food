package com.connectfood.auth.domain.model;

import java.util.UUID;

public record Role(
    UUID uuid,
    String name,
    String description
) {
}

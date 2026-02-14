package com.connectfood.auth.domain.model;

import java.util.Set;
import java.util.UUID;

public record User(
    Long id,
    UUID uuid,
    String email,
    String passwordHash,
    boolean enabled,
    Set<Role> roles
) {
}

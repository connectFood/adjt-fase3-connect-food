package com.connectfood.auth.entrypoint.rest.dto;

import java.util.Set;

public record MeResponse(
    String userUuid,
    Set<String> roles
) {
}

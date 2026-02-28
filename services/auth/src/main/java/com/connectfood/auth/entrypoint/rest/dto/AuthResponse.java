package com.connectfood.auth.entrypoint.rest.dto;

public record AuthResponse(
    String accessToken,
    long expiresInSeconds
) {
}

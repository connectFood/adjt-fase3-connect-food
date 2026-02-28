package com.connectfood.auth.entrypoint.rest.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresInSeconds
) {
}

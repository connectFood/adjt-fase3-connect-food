package com.connectfood.auth.application.dto;

public record AuthTokensOutput(
    String accessToken,
    long expiresInSeconds
) {
}

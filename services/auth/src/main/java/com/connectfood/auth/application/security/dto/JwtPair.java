package com.connectfood.auth.application.security.dto;

public record JwtPair(String accessToken, long expiresInSeconds) {
}

package com.connectfood.auth.application.security;

import java.util.Set;
import java.util.UUID;

public interface JwtIssuer {
  JwtPair issue(UUID userUuid, Set<String> roles);

  record JwtPair(String accessToken, String refreshToken, long expiresInSeconds) {
  }
}

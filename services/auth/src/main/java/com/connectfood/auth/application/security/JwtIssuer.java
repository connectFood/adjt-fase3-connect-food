package com.connectfood.auth.application.security;

import java.util.Set;
import java.util.UUID;

import com.connectfood.auth.application.security.dto.JwtPair;

public interface JwtIssuer {
  JwtPair issue(UUID userUuid, String email, Set<String> roles);
}

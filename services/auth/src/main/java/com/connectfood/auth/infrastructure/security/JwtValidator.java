package com.connectfood.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtValidator {

  private final String issuer;
  private final String secret;

  public JwtValidator(
      @Value("${auth.jwt.issuer}") String issuer,
      @Value("${auth.jwt.secret}") String secret
  ) {
    this.issuer = issuer;
    this.secret = secret;
  }

  public JwtPrincipal validateAndExtract(final String token) {
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    Claims claims = Jwts.parser()
        .verifyWith(key)
        .requireIssuer(issuer)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    var userUuid = UUID.fromString(claims.getSubject());

    Object rawRoles = claims.get("roles");
    Set<String> roles;

    if (rawRoles instanceof java.util.Collection<?> col) {
      roles = col.stream()
          .map(String::valueOf)
          .collect(java.util.stream.Collectors.toSet());
    } else {
      roles = java.util.Set.of();
    }

    return new JwtPrincipal(userUuid, roles);
  }

  public record JwtPrincipal(UUID userUuid, Set<String> roles) {
  }
}

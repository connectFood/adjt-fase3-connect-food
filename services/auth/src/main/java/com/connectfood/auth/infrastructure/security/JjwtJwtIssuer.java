package com.connectfood.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.connectfood.auth.application.security.JwtIssuer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JjwtJwtIssuer implements JwtIssuer {

  private final String issuer;
  private final String secret;
  private final long accessTtlMinutes;

  public JjwtJwtIssuer(
      @Value("${auth.jwt.issuer}") final String issuer,
      @Value("${auth.jwt.secret}") final String secret,
      @Value("${auth.jwt.access-token-ttl-minutes}") final long accessTtlMinutes
  ) {
    this.issuer = issuer;
    this.secret = secret;
    this.accessTtlMinutes = accessTtlMinutes;
  }

  @Override
  public JwtPair issue(final UUID userUuid, final Set<String> roles) {
    var now = Instant.now();
    var exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    var accessToken = Jwts.builder()
        .issuer(issuer)
        .subject(userUuid.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("roles", roles)
        .signWith(key)
        .compact();

    var refreshToken = UUID.randomUUID()
        .toString();

    return new JwtPair(accessToken, refreshToken, ChronoUnit.SECONDS.between(now, exp));
  }
}

package com.connectfood.auth.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.connectfood.auth.application.security.JwtIssuer;
import com.connectfood.auth.application.security.dto.JwtPair;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Component
public class JjwtJwtIssuer implements JwtIssuer {

  private final String issuer;
  private final String audience;
  private final SecretKey key;
  private final long accessTtlMinutes;

  public JjwtJwtIssuer(
      @Value("${auth.jwt.issuer}") final String issuer,
      @Value("${auth.jwt.audience}") String audience,
      @Value("${auth.jwt.secret}") final String secret,
      @Value("${auth.jwt.access-token-ttl-minutes}") final long accessTtlMinutes
  ) {
    this.issuer = issuer;
    this.audience = audience;
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTtlMinutes = accessTtlMinutes;
  }

  @Override
  public JwtPair issue(final UUID userUuid, final String email, final Set<String> roles) {
    var now = Instant.now();
    var exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

    var accessToken = Jwts.builder()
        .issuer(issuer)
        .audience()
        .add(audience)
        .and()
        .subject(userUuid.toString())
        .claim("email", email)
        .claim("roles", roles)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(key)
        .compact();

    return new JwtPair(accessToken, ChronoUnit.SECONDS.between(now, exp));
  }
}

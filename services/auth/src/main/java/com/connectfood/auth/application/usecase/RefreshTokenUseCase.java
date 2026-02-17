package com.connectfood.auth.application.usecase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import com.connectfood.auth.application.dto.AuthTokensOutput;
import com.connectfood.auth.application.dto.RefreshInput;
import com.connectfood.auth.application.security.JwtIssuer;
import com.connectfood.auth.application.security.RefreshTokenHash;
import com.connectfood.auth.domain.exception.UnauthorizedException;
import com.connectfood.auth.domain.port.RefreshTokenRepositoryPort;
import com.connectfood.auth.domain.port.UserRepositoryPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenUseCase {

  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final UserRepositoryPort userRepository;
  private final JwtIssuer jwtIssuer;
  private final long refreshTokenTtlDays;

  public RefreshTokenUseCase(
      RefreshTokenRepositoryPort refreshTokenRepository,
      UserRepositoryPort userRepository,
      JwtIssuer jwtIssuer,
      @Value("${auth.jwt.refresh-token-ttl-days}") long refreshTokenTtlDays
  ) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.jwtIssuer = jwtIssuer;
    this.refreshTokenTtlDays = refreshTokenTtlDays;
  }

  public AuthTokensOutput execute(RefreshInput input) {
    var now = Instant.now();
    var hash = RefreshTokenHash.sha256(input.refreshToken());

    var record = refreshTokenRepository.findValidByTokenHash(hash, now)
        .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

    // rotating refresh token
    refreshTokenRepository.revoke(record.uuid(), now);

    var user = userRepository.findByUuid(record.userUuid())
        .orElseThrow(() -> new UnauthorizedException("User not found"));

    if (!user.enabled()) {
      throw new UnauthorizedException("User disabled");
    }

    var roles = user.roles()
        .stream()
        .map(r -> r.name())
        .collect(Collectors.toSet());
    var pair = jwtIssuer.issue(user.uuid(), roles);

    var newHash = RefreshTokenHash.sha256(pair.refreshToken());
    var expiresAt = now.plus(refreshTokenTtlDays, ChronoUnit.DAYS);
    refreshTokenRepository.save(user.uuid(), newHash, expiresAt);

    return new AuthTokensOutput(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());
  }
}

package com.connectfood.auth.application.usecase;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import com.connectfood.auth.application.dto.AuthTokensOutput;
import com.connectfood.auth.application.dto.LoginInput;
import com.connectfood.auth.application.security.JwtIssuer;
import com.connectfood.auth.application.security.PasswordHasher;
import com.connectfood.auth.application.security.RefreshTokenHash;
import com.connectfood.auth.domain.exception.UnauthorizedException;
import com.connectfood.auth.domain.port.RefreshTokenRepositoryPort;
import com.connectfood.auth.domain.port.UserRepositoryPort;

public class LoginUseCase {

  private final UserRepositoryPort userRepository;
  private final PasswordHasher passwordHasher;
  private final JwtIssuer jwtIssuer;
  private final RefreshTokenRepositoryPort refreshTokenRepository;

  private final long refreshTokenTtlDays;

  public LoginUseCase(
      UserRepositoryPort userRepository,
      PasswordHasher passwordHasher,
      JwtIssuer jwtIssuer,
      RefreshTokenRepositoryPort refreshTokenRepository,
      long refreshTokenTtlDays
  ) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.jwtIssuer = jwtIssuer;
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenTtlDays = refreshTokenTtlDays;
  }

  public AuthTokensOutput execute(LoginInput input) {
    var user = userRepository.findByEmail(input.email())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!user.enabled()) {
      throw new UnauthorizedException("User disabled");
    }

    if (!passwordHasher.matches(input.password(), user.passwordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    var roles = user.roles()
        .stream()
        .map(r -> r.name())
        .collect(Collectors.toSet());
    var pair = jwtIssuer.issue(user.uuid(), roles);

    // persist refresh token (hash)
    var refreshHash = RefreshTokenHash.sha256(pair.refreshToken());
    var expiresAt = Instant.now()
        .plus(refreshTokenTtlDays, ChronoUnit.DAYS);
    refreshTokenRepository.save(user.uuid(), refreshHash, expiresAt);

    return new AuthTokensOutput(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());
  }
}

package com.connectfood.auth.application.usecase;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import com.connectfood.auth.application.dto.AuthTokensOutput;
import com.connectfood.auth.application.dto.RefreshInput;
import com.connectfood.auth.application.security.JwtIssuer;
import com.connectfood.auth.application.security.RefreshTokenHash;
import com.connectfood.auth.domain.exception.UnauthorizedException;
import com.connectfood.auth.domain.model.Role;
import com.connectfood.auth.domain.port.RefreshTokenRepositoryPort;
import com.connectfood.auth.domain.port.UserRepositoryPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RefreshTokenUseCase {

  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final UserRepositoryPort userRepository;
  private final JwtIssuer jwtIssuer;
  private final long refreshTokenTtlDays;

  public RefreshTokenUseCase(
      final RefreshTokenRepositoryPort refreshTokenRepository,
      final UserRepositoryPort userRepository,
      final JwtIssuer jwtIssuer,
      @Value("${auth.jwt.refresh-token-ttl-days}") final long refreshTokenTtlDays
  ) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
    this.jwtIssuer = jwtIssuer;
    this.refreshTokenTtlDays = refreshTokenTtlDays;
  }

  public AuthTokensOutput execute(final RefreshInput input) {
    log.info("I=Iniciando refresh token, refreshToken={}", input.refreshToken());
    var now = Instant.now();
    var hash = RefreshTokenHash.sha256(input.refreshToken());

    var record = refreshTokenRepository.findValidByTokenHash(hash, now)
        .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

    refreshTokenRepository.revoke(record.uuid(), now);

    var user = userRepository.findByUuid(record.userUuid())
        .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));

    if (!user.enabled()) {
      log.error("E=Usuario desabilitado, uuid={}", user.uuid());
      throw new UnauthorizedException("Usuário desabilitado");
    }

    var roles = user.roles()
        .stream()
        .map(Role::name)
        .collect(Collectors.toSet());

    var pair = jwtIssuer.issue(user.uuid(), roles);

    var newHash = RefreshTokenHash.sha256(pair.refreshToken());
    var expiresAt = now.plus(refreshTokenTtlDays, ChronoUnit.DAYS);
    refreshTokenRepository.save(user.uuid(), newHash, expiresAt);

    final var token = new AuthTokensOutput(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());

    log.info("I=Refresh token realizado com sucesso, email={}", user.email());
    return token;
  }
}

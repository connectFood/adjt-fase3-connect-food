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
import com.connectfood.auth.domain.model.Role;
import com.connectfood.auth.domain.port.RefreshTokenRepositoryPort;
import com.connectfood.auth.domain.port.UserRepositoryPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginUseCase {

  private final UserRepositoryPort userRepository;
  private final PasswordHasher passwordHasher;
  private final JwtIssuer jwtIssuer;
  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final long refreshTokenTtlDays;

  public LoginUseCase(
      final UserRepositoryPort userRepository,
      final PasswordHasher passwordHasher,
      final JwtIssuer jwtIssuer,
      final RefreshTokenRepositoryPort refreshTokenRepository,
      @Value("${auth.jwt.refresh-token-ttl-days}") final long refreshTokenTtlDays
  ) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.jwtIssuer = jwtIssuer;
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenTtlDays = refreshTokenTtlDays;
  }

  public AuthTokensOutput execute(final LoginInput input) {
    log.info("I=Iniciando login, email={}", input.email());
    var user = userRepository.findByEmail(input.email())
        .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

    if (!user.enabled()) {
      log.error("E=Usuario desabilitado, email={}", input.email());
      throw new UnauthorizedException("Usuário desabilitado");
    }

    if (!passwordHasher.matches(input.password(), user.passwordHash())) {
      log.error("E=Credenciais invalidas, email={}", input.email());
      throw new UnauthorizedException("Credenciais inválidas");
    }

    var roles = user.roles()
        .stream()
        .map(Role::name)
        .collect(Collectors.toSet());

    var pair = jwtIssuer.issue(user.uuid(), roles);

    var refreshHash = RefreshTokenHash.sha256(pair.refreshToken());
    var expiresAt = Instant.now()
        .plus(refreshTokenTtlDays, ChronoUnit.DAYS);

    refreshTokenRepository.save(user.uuid(), refreshHash, expiresAt);

    final var token = new AuthTokensOutput(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());

    log.info("I=Login realizado com sucesso, email={}", input.email());
    return token;
  }
}

package com.connectfood.auth.application.usecase;

import java.util.stream.Collectors;

import com.connectfood.auth.application.dto.AuthTokensOutput;
import com.connectfood.auth.application.dto.LoginInput;
import com.connectfood.auth.application.security.JwtIssuer;
import com.connectfood.auth.application.security.PasswordHasher;
import com.connectfood.auth.domain.exception.UnauthorizedException;
import com.connectfood.auth.domain.model.Role;
import com.connectfood.auth.domain.port.UserRepositoryPort;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoginUseCase {

  private final UserRepositoryPort userRepository;
  private final PasswordHasher passwordHasher;
  private final JwtIssuer jwtIssuer;

  public LoginUseCase(
      final UserRepositoryPort userRepository,
      final PasswordHasher passwordHasher,
      final JwtIssuer jwtIssuer
  ) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.jwtIssuer = jwtIssuer;
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

    var pair = jwtIssuer.issue(user.uuid(), user.email(), roles);

    final var token = new AuthTokensOutput(pair.accessToken(), pair.expiresInSeconds());

    log.info("I=Login realizado com sucesso, email={}", input.email());
    return token;
  }
}

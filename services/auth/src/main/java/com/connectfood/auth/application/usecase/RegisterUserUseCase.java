package com.connectfood.auth.application.usecase;

import java.util.Set;

import com.connectfood.auth.application.dto.RegisterUserInput;
import com.connectfood.auth.application.security.PasswordHasher;
import com.connectfood.auth.domain.exception.ConflictException;
import com.connectfood.auth.domain.exception.NotFoundException;
import com.connectfood.auth.domain.model.Role;
import com.connectfood.auth.domain.model.User;
import com.connectfood.auth.domain.port.RoleRepositoryPort;
import com.connectfood.auth.domain.port.UserRepositoryPort;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RegisterUserUseCase {

  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final PasswordHasher passwordHasher;

  public RegisterUserUseCase(
      final UserRepositoryPort userRepository,
      final RoleRepositoryPort roleRepository,
      final PasswordHasher passwordHasher
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordHasher = passwordHasher;
  }

  public User execute(final RegisterUserInput input) {
    log.info("I=Iniciando cadastro de usuario, email={}", input.email());
    validateExists(input.email());

    var role = roleRepository.findByName(input.roleName())
        .orElseThrow(() -> new NotFoundException("Role não encontrada: " + input.roleName()));

    final var user = userRepository.save(mapUser(input, role));

    log.info("I=Usuario cadastrado com sucesso, email={}", input.email());
    return user;
  }

  private void validateExists(final String email) {
    if (userRepository.existsByEmail(email)) {
      log.error("E=E-mail ja cadastrado, email={}", email);
      throw new ConflictException("E-mail já cadastrado");
    }
  }

  private User mapUser(final RegisterUserInput input, final Role role) {
    return new User(
        null,
        input.fullName(),
        input.email(),
        passwordHasher.hash(input.password()),
        true,
        Set.of(role)
    );
  }
}

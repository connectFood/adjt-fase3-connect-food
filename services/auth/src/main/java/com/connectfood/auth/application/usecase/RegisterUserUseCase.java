package com.connectfood.auth.application.usecase;

import java.util.Set;

import com.connectfood.auth.application.dto.RegisterUserInput;
import com.connectfood.auth.application.security.PasswordHasher;
import com.connectfood.auth.domain.exception.BadRequestException;
import com.connectfood.auth.domain.exception.NotFoundException;
import com.connectfood.auth.domain.model.User;
import com.connectfood.auth.domain.port.RoleRepositoryPort;
import com.connectfood.auth.domain.port.UserRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class RegisterUserUseCase {

  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final PasswordHasher passwordHasher;

  public RegisterUserUseCase(
      UserRepositoryPort userRepository,
      RoleRepositoryPort roleRepository,
      PasswordHasher passwordHasher
  ) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordHasher = passwordHasher;
  }

  public User execute(RegisterUserInput input) {
    if (userRepository.existsByEmail(input.email())) {
      throw new BadRequestException("Email already registered");
    }

    var role = roleRepository.findByName(input.roleName())
        .orElseThrow(() -> new NotFoundException("Role not found: " + input.roleName()));

    var user = new User(
        null,
        null,
        input.email(),
        passwordHasher.hash(input.password()),
        true,
        Set.of(role)
    );

    return userRepository.save(user);
  }
}

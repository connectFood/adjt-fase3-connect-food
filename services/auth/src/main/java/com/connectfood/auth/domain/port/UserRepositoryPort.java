package com.connectfood.auth.domain.port;

import java.util.Optional;
import java.util.UUID;

import com.connectfood.auth.domain.model.User;

public interface UserRepositoryPort {
  Optional<User> findByEmail(String email);

  Optional<User> findByUuid(UUID uuid);

  boolean existsByEmail(String email);

  User save(User user);
}

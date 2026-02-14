package com.connectfood.auth.domain.port;

import java.util.Optional;

import com.connectfood.auth.domain.model.Role;

public interface RoleRepositoryPort {
  Optional<Role> findByName(String name);

  Role save(Role role);
}

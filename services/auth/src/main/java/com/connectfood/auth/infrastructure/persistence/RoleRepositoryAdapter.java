package com.connectfood.auth.infrastructure.persistence;

import java.util.Optional;

import com.connectfood.auth.domain.model.Role;
import com.connectfood.auth.domain.port.RoleRepositoryPort;
import com.connectfood.auth.infrastructure.persistence.mapper.RoleInfraMapper;
import com.connectfood.auth.infrastructure.persistence.repository.JpaRoleRepository;

import org.springframework.stereotype.Component;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

  private final JpaRoleRepository jpa;

  public RoleRepositoryAdapter(final JpaRoleRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Optional<Role> findByName(String name) {
    return jpa.findByName(name)
        .map(RoleInfraMapper::toDomain);
  }
}

package com.connectfood.auth.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.connectfood.auth.infrastructure.persistence.entity.RoleEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRoleRepository extends JpaRepository<RoleEntity, Long> {
  Optional<RoleEntity> findByName(String name);

  Optional<RoleEntity> findByUuid(UUID uuid);
}

package com.connectfood.auth.infrastructure.persistence.repository;

import java.util.Optional;

import com.connectfood.auth.infrastructure.persistence.entity.RoleEntity;
import com.connectfood.auth.infrastructure.persistence.repository.common.BaseJpaRepository;

public interface JpaRoleRepository extends BaseJpaRepository<RoleEntity> {
  Optional<RoleEntity> findByName(String name);
}

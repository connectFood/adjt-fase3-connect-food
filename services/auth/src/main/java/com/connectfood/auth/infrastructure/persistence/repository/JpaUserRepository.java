package com.connectfood.auth.infrastructure.persistence.repository;

import java.util.Optional;

import com.connectfood.auth.infrastructure.persistence.entity.UserEntity;
import com.connectfood.auth.infrastructure.persistence.repository.common.BaseJpaRepository;

public interface JpaUserRepository extends BaseJpaRepository<UserEntity> {
  Optional<UserEntity> findByEmail(String email);

  boolean existsByEmail(String email);
}

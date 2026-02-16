package com.connectfood.auth.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.connectfood.auth.infrastructure.persistence.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByUuid(UUID uuid);

  boolean existsByEmail(String email);
}

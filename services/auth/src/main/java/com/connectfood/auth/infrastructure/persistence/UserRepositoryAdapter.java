package com.connectfood.auth.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.connectfood.auth.domain.model.User;
import com.connectfood.auth.domain.port.UserRepositoryPort;
import com.connectfood.auth.infrastructure.persistence.entity.UserEntity;
import com.connectfood.auth.infrastructure.persistence.mapper.UserInfraMapper;
import com.connectfood.auth.infrastructure.persistence.repository.JpaRoleRepository;
import com.connectfood.auth.infrastructure.persistence.repository.JpaUserRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final JpaUserRepository users;
  private final JpaRoleRepository roles;

  public UserRepositoryAdapter(JpaUserRepository users, JpaRoleRepository roles) {
    this.users = users;
    this.roles = roles;
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return users.findByEmail(email)
        .map(UserInfraMapper::toDomain);
  }

  @Override
  public Optional<User> findByUuid(UUID uuid) {
    return users.findByUuid(uuid)
        .map(UserInfraMapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return users.existsByEmail(email);
  }

  @Override
  @Transactional
  public User save(User user) {
    var now = OffsetDateTime.now();

    UserEntity entity;
    if (user.id() != null) {
      entity = users.findById(user.id())
          .orElseThrow();
    } else {
      entity = new UserEntity();
      entity.setUuid(UUID.randomUUID());
      entity.setCreatedAt(now);
      entity.setEnabled(true);
    }

    entity.setEmail(user.email());
    entity.setPasswordHash(user.passwordHash());
    entity.setEnabled(user.enabled());
    entity.setUpdatedAt(now);

    var roleEntities = user.roles()
        .stream()
        .map(r -> roles.findByUuid(r.uuid())
            .orElseThrow())
        .collect(Collectors.toSet());
    entity.setRoles(roleEntities);

    var saved = users.save(entity);
    return UserInfraMapper.toDomain(saved);
  }
}

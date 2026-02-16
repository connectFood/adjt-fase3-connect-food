package com.connectfood.auth.infrastructure.persistence.mapper;

import java.util.stream.Collectors;

import com.connectfood.auth.domain.model.User;
import com.connectfood.auth.infrastructure.persistence.entity.UserEntity;

public final class UserInfraMapper {
  private UserInfraMapper() {
  }

  public static User toDomain(UserEntity entity) {
    var roles = entity.getRoles()
        .stream()
        .map(RoleInfraMapper::toDomain)
        .collect(Collectors.toSet());

    return new User(
        entity.getId(),
        entity.getUuid(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.isEnabled(),
        roles
    );
  }
}

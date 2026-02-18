package com.connectfood.auth.infrastructure.persistence.mapper;

import com.connectfood.auth.domain.model.Role;
import com.connectfood.auth.infrastructure.persistence.entity.RoleEntity;

public final class RoleInfraMapper {

  private RoleInfraMapper() {
  }

  public static Role toDomain(final RoleEntity entity) {
    return new Role(entity.getUuid(), entity.getName(), entity.getDescription());
  }
}

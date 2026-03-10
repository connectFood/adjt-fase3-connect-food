package com.connectfood.restaurant.infrastructure.persistence.mapper;

import com.connectfood.restaurant.domain.model.Restaurant;
import com.connectfood.restaurant.infrastructure.persistence.entity.RestaurantEntity;

public final class RestaurantInfraMapper {

  private RestaurantInfraMapper() {
  }

  public static Restaurant toDomain(RestaurantEntity entity) {
    return new Restaurant(
        entity.getId(),
        entity.getUuid(),
        entity.getName(),
        entity.getDescription(),
        entity.isActive()
    );
  }

  public static RestaurantEntity toEntity(Restaurant domain) {
    var entity = new RestaurantEntity();
    entity.setId(domain.id());
    entity.setUuid(domain.uuid());
    entity.setName(domain.name());
    entity.setDescription(domain.description());
    entity.setActive(domain.active());
    return entity;
  }
}

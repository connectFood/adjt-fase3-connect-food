package com.connectfood.restaurant.infrastructure.persistence.mapper;

import com.connectfood.restaurant.domain.model.MenuItem;
import com.connectfood.restaurant.infrastructure.persistence.entity.MenuItemEntity;

public final class MenuItemInfraMapper {

  private MenuItemInfraMapper() {
  }

  public static MenuItem toDomain(MenuItemEntity entity) {
    return new MenuItem(
        entity.getId(),
        entity.getUuid(),
        entity.getRestaurantUuid(),
        entity.getItemCode(),
        entity.getName(),
        entity.getDescription(),
        entity.getPrice(),
        entity.isAvailable()
    );
  }

  public static MenuItemEntity toEntity(MenuItem domain) {
    var entity = new MenuItemEntity();
    entity.setId(domain.id());
    entity.setUuid(domain.uuid());
    entity.setRestaurantUuid(domain.restaurantUuid());
    entity.setItemCode(domain.itemCode());
    entity.setName(domain.name());
    entity.setDescription(domain.description());
    entity.setPrice(domain.price());
    entity.setAvailable(domain.available());
    return entity;
  }
}

package com.connectfood.restaurant.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.restaurant.infrastructure.persistence.entity.MenuItemEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMenuItemRepository extends JpaRepository<MenuItemEntity, Long> {
  Optional<MenuItemEntity> findByUuid(UUID uuid);

  List<MenuItemEntity> findByRestaurantUuidOrderByNameAsc(UUID restaurantUuid);
}

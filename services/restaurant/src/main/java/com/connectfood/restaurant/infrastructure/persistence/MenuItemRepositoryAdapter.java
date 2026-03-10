package com.connectfood.restaurant.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.restaurant.domain.model.MenuItem;
import com.connectfood.restaurant.domain.port.MenuItemRepositoryPort;
import com.connectfood.restaurant.infrastructure.persistence.mapper.MenuItemInfraMapper;
import com.connectfood.restaurant.infrastructure.persistence.repository.JpaMenuItemRepository;

import org.springframework.stereotype.Component;

@Component
public class MenuItemRepositoryAdapter implements MenuItemRepositoryPort {

  private final JpaMenuItemRepository jpaRepository;

  public MenuItemRepositoryAdapter(JpaMenuItemRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public MenuItem save(MenuItem item) {
    var saved = jpaRepository.save(MenuItemInfraMapper.toEntity(item));
    return MenuItemInfraMapper.toDomain(saved);
  }

  @Override
  public Optional<MenuItem> findByUuid(UUID uuid) {
    return jpaRepository.findByUuid(uuid)
        .map(MenuItemInfraMapper::toDomain);
  }

  @Override
  public List<MenuItem> findByRestaurantUuid(UUID restaurantUuid) {
    return jpaRepository.findByRestaurantUuidOrderByNameAsc(restaurantUuid)
        .stream()
        .map(MenuItemInfraMapper::toDomain)
        .toList();
  }
}

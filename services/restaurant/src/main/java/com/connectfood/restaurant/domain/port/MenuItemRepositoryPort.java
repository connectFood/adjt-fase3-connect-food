package com.connectfood.restaurant.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.restaurant.domain.model.MenuItem;

public interface MenuItemRepositoryPort {
  MenuItem save(MenuItem item);

  Optional<MenuItem> findByUuid(UUID uuid);

  List<MenuItem> findByRestaurantUuid(UUID restaurantUuid);
}

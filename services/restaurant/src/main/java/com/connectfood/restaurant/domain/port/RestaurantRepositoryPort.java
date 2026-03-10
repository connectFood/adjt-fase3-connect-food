package com.connectfood.restaurant.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.restaurant.domain.model.Restaurant;

public interface RestaurantRepositoryPort {
  Restaurant save(Restaurant restaurant);

  Optional<Restaurant> findByUuid(UUID uuid);

  List<Restaurant> findAll();
}

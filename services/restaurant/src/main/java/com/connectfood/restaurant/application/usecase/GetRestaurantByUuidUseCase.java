package com.connectfood.restaurant.application.usecase;

import java.util.UUID;

import com.connectfood.restaurant.application.dto.RestaurantOutput;
import com.connectfood.restaurant.domain.exception.NotFoundException;
import com.connectfood.restaurant.domain.port.RestaurantRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class GetRestaurantByUuidUseCase {

  private final RestaurantRepositoryPort repository;

  public GetRestaurantByUuidUseCase(RestaurantRepositoryPort repository) {
    this.repository = repository;
  }

  public RestaurantOutput execute(UUID restaurantUuid) {
    var restaurant = repository.findByUuid(restaurantUuid)
        .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantUuid));

    return new RestaurantOutput(
        restaurant.uuid(),
        restaurant.name(),
        restaurant.description(),
        restaurant.active()
    );
  }
}

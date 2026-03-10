package com.connectfood.restaurant.application.usecase;

import com.connectfood.restaurant.application.dto.CreateRestaurantInput;
import com.connectfood.restaurant.application.dto.RestaurantOutput;
import com.connectfood.restaurant.domain.exception.BadRequestException;
import com.connectfood.restaurant.domain.model.Restaurant;
import com.connectfood.restaurant.domain.port.RestaurantRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class CreateRestaurantUseCase {

  private final RestaurantRepositoryPort repository;

  public CreateRestaurantUseCase(RestaurantRepositoryPort repository) {
    this.repository = repository;
  }

  public RestaurantOutput execute(CreateRestaurantInput input) {
    if (input.name() == null || input.name().isBlank()) {
      throw new BadRequestException("Restaurant name is required");
    }

    var restaurant = new Restaurant(
        null,
        null,
        input.name().trim(),
        input.description(),
        true
    );

    return toOutput(repository.save(restaurant));
  }

  private RestaurantOutput toOutput(Restaurant restaurant) {
    return new RestaurantOutput(
        restaurant.uuid(),
        restaurant.name(),
        restaurant.description(),
        restaurant.active()
    );
  }
}

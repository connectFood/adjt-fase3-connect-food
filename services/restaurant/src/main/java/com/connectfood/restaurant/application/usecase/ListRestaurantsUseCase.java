package com.connectfood.restaurant.application.usecase;

import java.util.List;

import com.connectfood.restaurant.application.dto.RestaurantOutput;
import com.connectfood.restaurant.domain.port.RestaurantRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class ListRestaurantsUseCase {

  private final RestaurantRepositoryPort repository;

  public ListRestaurantsUseCase(RestaurantRepositoryPort repository) {
    this.repository = repository;
  }

  public List<RestaurantOutput> execute() {
    return repository.findAll()
        .stream()
        .map(r -> new RestaurantOutput(r.uuid(), r.name(), r.description(), r.active()))
        .toList();
  }
}

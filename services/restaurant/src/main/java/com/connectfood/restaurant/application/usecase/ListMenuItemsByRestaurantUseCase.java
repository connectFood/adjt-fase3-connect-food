package com.connectfood.restaurant.application.usecase;

import java.util.List;
import java.util.UUID;

import com.connectfood.restaurant.application.dto.MenuItemOutput;
import com.connectfood.restaurant.domain.exception.NotFoundException;
import com.connectfood.restaurant.domain.port.MenuItemRepositoryPort;
import com.connectfood.restaurant.domain.port.RestaurantRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class ListMenuItemsByRestaurantUseCase {

  private final RestaurantRepositoryPort restaurantRepository;
  private final MenuItemRepositoryPort itemRepository;

  public ListMenuItemsByRestaurantUseCase(
      RestaurantRepositoryPort restaurantRepository,
      MenuItemRepositoryPort itemRepository
  ) {
    this.restaurantRepository = restaurantRepository;
    this.itemRepository = itemRepository;
  }

  public List<MenuItemOutput> execute(UUID restaurantUuid) {
    restaurantRepository.findByUuid(restaurantUuid)
        .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantUuid));

    return itemRepository.findByRestaurantUuid(restaurantUuid)
        .stream()
        .map(i -> new MenuItemOutput(
            i.uuid(),
            i.restaurantUuid(),
            i.itemCode(),
            i.name(),
            i.description(),
            i.price(),
            i.available()
        ))
        .toList();
  }
}

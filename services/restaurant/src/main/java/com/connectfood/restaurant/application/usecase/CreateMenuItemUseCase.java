package com.connectfood.restaurant.application.usecase;

import java.util.UUID;

import com.connectfood.restaurant.application.dto.CreateMenuItemInput;
import com.connectfood.restaurant.application.dto.MenuItemOutput;
import com.connectfood.restaurant.domain.exception.BadRequestException;
import com.connectfood.restaurant.domain.exception.NotFoundException;
import com.connectfood.restaurant.domain.model.MenuItem;
import com.connectfood.restaurant.domain.port.MenuItemRepositoryPort;
import com.connectfood.restaurant.domain.port.RestaurantRepositoryPort;

import org.springframework.stereotype.Service;

@Service
public class CreateMenuItemUseCase {

  private final RestaurantRepositoryPort restaurantRepository;
  private final MenuItemRepositoryPort itemRepository;

  public CreateMenuItemUseCase(
      RestaurantRepositoryPort restaurantRepository,
      MenuItemRepositoryPort itemRepository
  ) {
    this.restaurantRepository = restaurantRepository;
    this.itemRepository = itemRepository;
  }

  public MenuItemOutput execute(UUID restaurantUuid, CreateMenuItemInput input) {
    restaurantRepository.findByUuid(restaurantUuid)
        .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantUuid));

    if (input.price() == null || input.price().signum() <= 0) {
      throw new BadRequestException("Menu item price must be greater than zero");
    }

    var saved = itemRepository.save(new MenuItem(
        null,
        null,
        restaurantUuid,
        input.itemCode(),
        input.name(),
        input.description(),
        input.price(),
        input.available()
    ));

    return toOutput(saved);
  }

  private MenuItemOutput toOutput(MenuItem item) {
    return new MenuItemOutput(
        item.uuid(),
        item.restaurantUuid(),
        item.itemCode(),
        item.name(),
        item.description(),
        item.price(),
        item.available()
    );
  }
}

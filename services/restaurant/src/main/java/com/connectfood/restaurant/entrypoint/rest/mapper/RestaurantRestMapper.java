package com.connectfood.restaurant.entrypoint.rest.mapper;

import com.connectfood.restaurant.application.dto.CreateMenuItemInput;
import com.connectfood.restaurant.application.dto.CreateRestaurantInput;
import com.connectfood.restaurant.entrypoint.rest.dto.CreateMenuItemRequest;
import com.connectfood.restaurant.entrypoint.rest.dto.CreateRestaurantRequest;

public final class RestaurantRestMapper {

  private RestaurantRestMapper() {
  }

  public static CreateRestaurantInput toInput(CreateRestaurantRequest request) {
    return new CreateRestaurantInput(request.name(), request.description());
  }

  public static CreateMenuItemInput toInput(CreateMenuItemRequest request) {
    return new CreateMenuItemInput(
        request.itemCode(),
        request.name(),
        request.description(),
        request.price(),
        request.available()
    );
  }
}

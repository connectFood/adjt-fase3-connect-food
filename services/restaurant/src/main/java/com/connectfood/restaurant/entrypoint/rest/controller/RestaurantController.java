package com.connectfood.restaurant.entrypoint.rest.controller;

import java.util.UUID;

import com.connectfood.restaurant.application.usecase.CreateMenuItemUseCase;
import com.connectfood.restaurant.application.usecase.CreateRestaurantUseCase;
import com.connectfood.restaurant.application.usecase.GetRestaurantByUuidUseCase;
import com.connectfood.restaurant.application.usecase.ListMenuItemsByRestaurantUseCase;
import com.connectfood.restaurant.application.usecase.ListRestaurantsUseCase;
import com.connectfood.restaurant.entrypoint.rest.dto.CreateMenuItemRequest;
import com.connectfood.restaurant.entrypoint.rest.dto.CreateRestaurantRequest;
import com.connectfood.restaurant.entrypoint.rest.mapper.RestaurantRestMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

  private final CreateRestaurantUseCase createRestaurantUseCase;
  private final ListRestaurantsUseCase listRestaurantsUseCase;
  private final GetRestaurantByUuidUseCase getRestaurantByUuidUseCase;
  private final CreateMenuItemUseCase createMenuItemUseCase;
  private final ListMenuItemsByRestaurantUseCase listMenuItemsByRestaurantUseCase;

  public RestaurantController(
      CreateRestaurantUseCase createRestaurantUseCase,
      ListRestaurantsUseCase listRestaurantsUseCase,
      GetRestaurantByUuidUseCase getRestaurantByUuidUseCase,
      CreateMenuItemUseCase createMenuItemUseCase,
      ListMenuItemsByRestaurantUseCase listMenuItemsByRestaurantUseCase
  ) {
    this.createRestaurantUseCase = createRestaurantUseCase;
    this.listRestaurantsUseCase = listRestaurantsUseCase;
    this.getRestaurantByUuidUseCase = getRestaurantByUuidUseCase;
    this.createMenuItemUseCase = createMenuItemUseCase;
    this.listMenuItemsByRestaurantUseCase = listMenuItemsByRestaurantUseCase;
  }

  @PostMapping
  public ResponseEntity<?> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
    var out = createRestaurantUseCase.execute(RestaurantRestMapper.toInput(request));
    return ResponseEntity.status(201).body(out);
  }

  @GetMapping
  public ResponseEntity<?> listRestaurants() {
    return ResponseEntity.ok(listRestaurantsUseCase.execute());
  }

  @GetMapping("/{restaurantUuid}")
  public ResponseEntity<?> getRestaurant(@PathVariable UUID restaurantUuid) {
    return ResponseEntity.ok(getRestaurantByUuidUseCase.execute(restaurantUuid));
  }

  @PostMapping("/{restaurantUuid}/items")
  public ResponseEntity<?> createMenuItem(
      @PathVariable UUID restaurantUuid,
      @Valid @RequestBody CreateMenuItemRequest request
  ) {
    var out = createMenuItemUseCase.execute(restaurantUuid, RestaurantRestMapper.toInput(request));
    return ResponseEntity.status(201).body(out);
  }

  @GetMapping("/{restaurantUuid}/items")
  public ResponseEntity<?> listMenuItems(@PathVariable UUID restaurantUuid) {
    return ResponseEntity.ok(listMenuItemsByRestaurantUseCase.execute(restaurantUuid));
  }
}

package com.connectfood.order.entrypoint.rest.mapper;

import com.connectfood.order.application.dto.CreateOrderInput;
import com.connectfood.order.entrypoint.rest.dto.CreateOrderRequest;

public final class OrderRestMapper {
  private OrderRestMapper() {
  }

  public static CreateOrderInput toInput(CreateOrderRequest request) {
    return new CreateOrderInput(
        request.restaurantId(),
        request.items()
            .stream()
            .map(i -> new CreateOrderInput.CreateOrderItemInput(i.itemId(), i.itemName(), i.quantity(), i.unitPrice()))
            .toList()
    );
  }
}

package com.connectfood.restaurant.entrypoint.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRestaurantRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 512) String description
) {
}

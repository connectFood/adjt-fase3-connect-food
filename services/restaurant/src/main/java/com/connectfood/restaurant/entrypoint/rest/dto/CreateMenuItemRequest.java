package com.connectfood.restaurant.entrypoint.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMenuItemRequest(
    @NotBlank @Size(max = 64) String itemCode,
    @NotBlank @Size(max = 120) String name,
    @Size(max = 512) String description,
    @NotNull @Positive BigDecimal price,
    @NotNull Boolean available
) {
}

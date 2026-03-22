package com.connectfood.auth.entrypoint.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(min = 3, max = 150) String name,
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @JsonAlias("role")
    @NotBlank String roleName
) {
}

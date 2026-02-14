package com.connectfood.auth.application.dto;

public record RegisterUserInput(
    String email,
    String password,
    String roleName
) {
}

package com.connectfood.auth.application.dto;

public record RegisterUserInput(
    String fullName,
    String email,
    String password,
    String roleName
) {
}

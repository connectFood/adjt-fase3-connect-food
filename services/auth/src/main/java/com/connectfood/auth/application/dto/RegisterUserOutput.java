package com.connectfood.auth.application.dto;

public record RegisterUserOutput(
    String uuid,
    String name,
    String email,
    String role
) {
}

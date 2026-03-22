package com.connectfood.auth.entrypoint.rest.dto;

public record RegisterResponse(
    String uuid,
    String name,
    String email,
    String role
) {
}

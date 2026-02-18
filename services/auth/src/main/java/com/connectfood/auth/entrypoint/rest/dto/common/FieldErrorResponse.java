package com.connectfood.auth.entrypoint.rest.dto.common;

public record FieldErrorResponse(
    String field,
    String message
) {
}

package com.connectfood.payment.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProcpagResponse(
    Object any
) {
}

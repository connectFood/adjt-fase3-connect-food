package com.connectfood.payment.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentTransaction(
    Long id,
    UUID uuid,
    UUID orderUuid,
    UUID customerUuid,
    PaymentStatus status,
    BigDecimal amount
) {
}

package com.connectfood.order.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentApprovedEvent(
    UUID paymentUuid,
    UUID orderUuid,
    UUID customerUuid,
    BigDecimal amount
) {
}

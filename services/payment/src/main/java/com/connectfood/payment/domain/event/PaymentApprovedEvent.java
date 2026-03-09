package com.connectfood.payment.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentApprovedEvent(
    UUID paymentUuid,
    UUID orderUuid,
    UUID customerUuid,
    BigDecimal amount
) {
}

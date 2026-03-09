package com.connectfood.payment.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPaymentCommand(
    UUID orderUuid,
    UUID customerUuid,
    BigDecimal amount
) {
}

package com.connectfood.payment.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record PendingPaymentCommand(
    UUID paymentUuid,
    UUID orderUuid,
    UUID customerUuid,
    BigDecimal amount,
    String reason
) {
}

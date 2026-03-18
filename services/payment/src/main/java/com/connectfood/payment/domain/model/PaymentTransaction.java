package com.connectfood.payment.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentTransaction(
    Long id,
    UUID uuid,
    UUID orderUuid,
    UUID customerUuid,
    PaymentStatus status,
    BigDecimal amount,
    int pendingReprocessAttempts
) {
  public static PaymentTransaction newPending(UUID paymentUuid, UUID orderUuid, UUID customerUuid, BigDecimal amount) {
    return new PaymentTransaction(
        null,
        paymentUuid == null ? UUID.randomUUID() : paymentUuid,
        orderUuid,
        customerUuid,
        PaymentStatus.PENDING,
        amount,
        0
    );
  }

  public PaymentTransaction withStatus(PaymentStatus newStatus) {
    return new PaymentTransaction(id, uuid, orderUuid, customerUuid, newStatus, amount, pendingReprocessAttempts);
  }

  public PaymentTransaction incrementPendingReprocessAttempts() {
    return new PaymentTransaction(id, uuid, orderUuid, customerUuid, status, amount, pendingReprocessAttempts + 1);
  }

  public PaymentTransaction resetPendingReprocessAttempts() {
    return new PaymentTransaction(id, uuid, orderUuid, customerUuid, status, amount, 0);
  }
}

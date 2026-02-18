package com.connectfood.payment.infrastructure.persistence.mapper;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.infrastructure.persistence.entity.PaymentTransactionEntity;

public final class PaymentInfraMapper {
  private PaymentInfraMapper() {
  }

  public static PaymentTransaction toDomain(PaymentTransactionEntity e) {
    return new PaymentTransaction(
        e.getId(),
        e.getUuid(),
        e.getOrderUuid(),
        e.getCustomerUuid(),
        PaymentStatus.valueOf(e.getStatus()),
        e.getAmount()
    );
  }

  public static PaymentTransactionEntity toEntityForInsert(PaymentTransaction tx) {
    var now = OffsetDateTime.now();

    var e = new PaymentTransactionEntity();
    e.setUuid(tx.uuid() == null ? UUID.randomUUID() : tx.uuid());
    e.setOrderUuid(tx.orderUuid());
    e.setCustomerUuid(tx.customerUuid());
    e.setStatus(tx.status()
        .name());
    e.setAmount(tx.amount());
    e.setCreatedAt(now);
    e.setUpdatedAt(now);
    return e;
  }

  public static void applyStatusUpdate(PaymentTransactionEntity e, PaymentStatus status) {
    e.setStatus(status.name());
    e.setUpdatedAt(OffsetDateTime.now());
  }
}

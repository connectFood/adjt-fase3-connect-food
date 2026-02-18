package com.connectfood.payment.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.connectfood.payment.infrastructure.persistence.entity.PaymentTransactionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {
  Optional<PaymentTransactionEntity> findByOrderUuid(UUID orderUuid);
}

package com.connectfood.payment.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.domain.port.PaymentTransactionRepositoryPort;
import com.connectfood.payment.infrastructure.persistence.mapper.PaymentInfraMapper;
import com.connectfood.payment.infrastructure.persistence.repository.JpaPaymentTransactionRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentTransactionRepositoryAdapter implements PaymentTransactionRepositoryPort {

  private final JpaPaymentTransactionRepository jpa;

  public PaymentTransactionRepositoryAdapter(JpaPaymentTransactionRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional
  public PaymentTransaction save(PaymentTransaction tx) {
    var saved = jpa.save(PaymentInfraMapper.toEntityForInsert(tx));
    return PaymentInfraMapper.toDomain(saved);
  }

  @Override
  public Optional<PaymentTransaction> findByOrderUuid(UUID orderUuid) {
    return jpa.findByOrderUuid(orderUuid)
        .map(PaymentInfraMapper::toDomain);
  }

  @Transactional
  public void updateStatusByOrderUuid(UUID orderUuid, PaymentStatus status) {
    jpa.findByOrderUuid(orderUuid)
        .ifPresent(e -> {
          PaymentInfraMapper.applyStatusUpdate(e, status);
          jpa.save(e);
        });
  }
}

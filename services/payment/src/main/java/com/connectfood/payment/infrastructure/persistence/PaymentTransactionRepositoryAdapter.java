package com.connectfood.payment.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

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
    var existing = jpa.findByOrderUuid(tx.orderUuid());

    if (existing.isPresent()) {
      var entity = existing.get();
      PaymentInfraMapper.applyDomain(entity, tx);
      return PaymentInfraMapper.toDomain(jpa.save(entity));
    }

    var saved = jpa.save(PaymentInfraMapper.toEntityForInsert(tx));
    return PaymentInfraMapper.toDomain(saved);
  }

  @Override
  public Optional<PaymentTransaction> findByOrderUuid(UUID orderUuid) {
    return jpa.findByOrderUuid(orderUuid).map(PaymentInfraMapper::toDomain);
  }
}
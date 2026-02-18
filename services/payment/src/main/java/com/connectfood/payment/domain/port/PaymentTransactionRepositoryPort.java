package com.connectfood.payment.domain.port;

import com.connectfood.payment.domain.model.PaymentTransaction;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepositoryPort {
  PaymentTransaction save(PaymentTransaction tx);

  Optional<PaymentTransaction> findByOrderUuid(UUID orderUuid);
}

package com.connectfood.payment.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProcessorPort {
  PaymentProcessorResult process(UUID orderUuid, UUID customerUuid, BigDecimal amount);

  record PaymentProcessorResult(boolean approved) {
  }
}

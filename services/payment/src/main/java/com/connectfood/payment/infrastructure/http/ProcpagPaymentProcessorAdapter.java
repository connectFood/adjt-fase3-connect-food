package com.connectfood.payment.infrastructure.http;

import java.math.BigDecimal;
import java.util.UUID;

import com.connectfood.payment.domain.port.PaymentProcessorPort;
import com.connectfood.payment.infrastructure.http.dto.ProcpagRequest;

import org.springframework.stereotype.Component;

@Component
public class ProcpagPaymentProcessorAdapter implements PaymentProcessorPort {

  private final ProcpagFeignClient feign;

  public ProcpagPaymentProcessorAdapter(ProcpagFeignClient feign) {
    this.feign = feign;
  }

  @Override
  public PaymentProcessorResult process(UUID orderUuid, UUID customerUuid, BigDecimal amount) {
    feign.requisicao(new ProcpagRequest(
        amount,
        orderUuid.toString(),
        customerUuid.toString()
    ));

    return new PaymentProcessorResult(true);
  }
}

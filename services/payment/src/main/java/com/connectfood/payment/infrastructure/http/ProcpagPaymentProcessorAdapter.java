package com.connectfood.payment.infrastructure.http;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import com.connectfood.payment.domain.port.PaymentProcessorPort;
import com.connectfood.payment.infrastructure.http.dto.ProcpagRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProcpagPaymentProcessorAdapter implements PaymentProcessorPort {

  private final ProcpagFeignClient feign;

  public ProcpagPaymentProcessorAdapter(ProcpagFeignClient feign) {
    this.feign = feign;
  }

  @Override
  public PaymentProcessorResult process(UUID orderUuid, UUID customerUuid, BigDecimal amount) {
    log.info("I=Enviando requisicao para Procpag. Pedido={} Cliente={} Valor={}", orderUuid, customerUuid, amount);
    var response = feign.requisicao(new ProcpagRequest(
        amount,
        orderUuid.toString(),
        customerUuid.toString()
    ));

    var status = response == null || response.status() == null
        ? "accepted"
        : response.status().trim().toLowerCase(Locale.ROOT);

    if ("accepted".equals(status) || "approved".equals(status) || "pago".equals(status)) {
      log.info("I=Resposta Procpag aprovada para pedido {} com status {}", orderUuid, status);
      return PaymentProcessorResult.approvedResult();
    }

    log.warn("W=Resposta Procpag nao aprovada para pedido {} com status {}", orderUuid, status);
    return PaymentProcessorResult.pendingResult("NOT_APPROVED");
  }
}

package com.connectfood.payment.infrastructure.http;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

import com.connectfood.payment.domain.port.PaymentProcessorPort;
import com.connectfood.payment.infrastructure.http.dto.ProcpagRequest;
import feign.FeignException;

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
    var valorInteiro = toProcpagValor(amount);
    log.info(
        "I=Enviando requisicao para Procpag. Pedido={} Cliente={} ValorOriginal={} ValorEnviado={}",
        orderUuid,
        customerUuid,
        amount,
        valorInteiro
    );
    try {
      var response = feign.requisicao(new ProcpagRequest(valorInteiro, orderUuid.toString(), customerUuid.toString()));
      var status = response == null || response.status() == null
          ? "accepted"
          : response.status().trim().toLowerCase(Locale.ROOT);

      log.info("I=Retorno da API Procpag recebido. Pedido={} payload_status={} status_normalizado={}",
          orderUuid, response == null ? "null" : response.status(), status);

      if ("accepted".equals(status) || "approved".equals(status) || "pago".equals(status)) {
        log.info("I=Resultado Procpag=APROVADO para pedido {}", orderUuid);
        return PaymentProcessorResult.approvedResult();
      }

      log.warn("W=Resultado Procpag=NAO_APROVADO para pedido {} status={}. Nova tentativa sera executada", orderUuid, status);
      throw new IllegalStateException("PROCPAG_STATUS_NOT_APPROVED");
    } catch (FeignException ex) {
      log.warn(
          "W=Falha HTTP na Procpag para pedido {} http_status={} response_body={}. Nova tentativa sera executada",
          orderUuid,
          ex.status(),
          ex.contentUTF8()
      );
      throw new IllegalStateException("PROCPAG_HTTP_ERROR");
    } catch (Exception ex) {
      if (ex instanceof IllegalStateException) {
        throw ex;
      }
      log.warn("W=Falha inesperada na Procpag para pedido {} mensagem={}. Nova tentativa sera executada", orderUuid, ex.getMessage());
      throw new IllegalStateException("PROCPAG_UNEXPECTED_ERROR");
    }
  }

  private long toProcpagValor(BigDecimal amount) {
    if (amount == null) {
      throw new IllegalArgumentException("VALOR_PAGAMENTO_OBRIGATORIO");
    }

    var valorNormalizado = amount.setScale(0, RoundingMode.HALF_UP);
    if (valorNormalizado.compareTo(amount) != 0) {
      log.warn(
          "W=Valor com casas decimais para Procpag foi arredondado. valor_original={} valor_normalizado={}",
          amount,
          valorNormalizado
      );
    }
    return valorNormalizado.longValueExact();
  }
}

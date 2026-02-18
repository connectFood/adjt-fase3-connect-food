package com.connectfood.payment.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProcpagRequest(
    @JsonProperty("valor") BigDecimal valor,
    @JsonProperty("pagamento_id") String pagamentoId,
    @JsonProperty("cliente_id") String clienteId
) {
}

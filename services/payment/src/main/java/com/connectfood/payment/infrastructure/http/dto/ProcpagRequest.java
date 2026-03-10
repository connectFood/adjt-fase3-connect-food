package com.connectfood.payment.infrastructure.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProcpagRequest(
    @JsonProperty("valor") Long valor,
    @JsonProperty("pagamento_id") String pagamentoId,
    @JsonProperty("cliente_id") String clienteId
) {
}

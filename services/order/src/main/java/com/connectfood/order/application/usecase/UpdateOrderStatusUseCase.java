package com.connectfood.order.application.usecase;

import java.util.UUID;

import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.domain.port.OrderRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdateOrderStatusUseCase {

  private final OrderRepositoryPort repository;

  public UpdateOrderStatusUseCase(OrderRepositoryPort repository) {
    this.repository = repository;
  }

  public boolean execute(UUID orderUuid, OrderStatus status) {
    log.info("I=Iniciando atualização de status do pedido orderUuid={} novoStatus={}", orderUuid, status);
    var updated = repository.updateStatusByUuid(orderUuid, status);
    if (updated) {
      log.info("I=Status do pedido atualizado com sucesso orderUuid={} novoStatus={}", orderUuid, status);
    } else {
      log.warn("W=Pedido não encontrado para atualização de status orderUuid={} novoStatus={}", orderUuid, status);
    }
    return updated;
  }
}

package com.connectfood.payment.application.usecase;

import java.util.UUID;

import com.connectfood.payment.application.command.OrderPaymentCommand;
import com.connectfood.payment.application.service.PaymentOrchestratorService;
import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.domain.port.PaymentTransactionRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessPaymentOnOrderCreatedUseCase {

  private final PaymentTransactionRepositoryPort repository;
  private final PaymentOrchestratorService paymentOrchestrator;

  public ProcessPaymentOnOrderCreatedUseCase(
      PaymentTransactionRepositoryPort repository,
      PaymentOrchestratorService paymentOrchestrator
  ) {
    this.repository = repository;
    this.paymentOrchestrator = paymentOrchestrator;
  }

  public void execute(OrderPaymentCommand command) {
    log.info("I=Use case de pagamento iniciado para pedido {}", command.orderUuid());
    var existing = repository.findByOrderUuid(command.orderUuid());
    if (existing.isPresent() && existing.get().status() == PaymentStatus.APPROVED) {
      log.warn("W=Pedido {} ja possui pagamento aprovado. Processamento ignorado", command.orderUuid());
      return;
    }

    var transaction = existing.orElseGet(() -> repository.save(PaymentTransaction.newPending(
        UUID.randomUUID(),
        command.orderUuid(),
        command.customerUuid(),
        command.amount()
    )));

    if (existing.isEmpty()) {
      log.info("I=Transacao de pagamento criada com status pendente para pedido {}", command.orderUuid());
    }

    paymentOrchestrator.processPayment(transaction);
    log.info("I=Use case de pagamento finalizado para pedido {}", command.orderUuid());
  }
}

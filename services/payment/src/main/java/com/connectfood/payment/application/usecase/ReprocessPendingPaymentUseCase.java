package com.connectfood.payment.application.usecase;

import com.connectfood.payment.application.command.PendingPaymentCommand;
import com.connectfood.payment.application.service.PaymentOrchestratorService;
import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.domain.port.PaymentTransactionRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReprocessPendingPaymentUseCase {

  private final PaymentTransactionRepositoryPort repository;
  private final PaymentOrchestratorService paymentOrchestrator;

  public ReprocessPendingPaymentUseCase(
      PaymentTransactionRepositoryPort repository,
      PaymentOrchestratorService paymentOrchestrator
  ) {
    this.repository = repository;
    this.paymentOrchestrator = paymentOrchestrator;
  }

  public void execute(PendingPaymentCommand command) {
    log.info("I=Use case de reprocessamento iniciado para pedido {}", command.orderUuid());
    var transaction = repository.findByOrderUuid(command.orderUuid())
        .orElseGet(() -> repository.save(PaymentTransaction.newPending(
            command.paymentUuid(),
            command.orderUuid(),
            command.customerUuid(),
            command.amount()
        )));

    if (transaction.status() == PaymentStatus.APPROVED) {
      log.warn("W=Pedido {} ja possui pagamento aprovado. Reprocessamento ignorado", command.orderUuid());
      return;
    }

    paymentOrchestrator.processPayment(transaction.withStatus(PaymentStatus.PENDING));
    log.info("I=Use case de reprocessamento finalizado para pedido {}", command.orderUuid());
  }
}

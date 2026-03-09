package com.connectfood.payment.application.service;

import com.connectfood.payment.domain.event.PaymentApprovedEvent;
import com.connectfood.payment.domain.event.PaymentPendingEvent;
import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.domain.port.PaymentEventPublisherPort;
import com.connectfood.payment.domain.port.PaymentProcessorPort;
import com.connectfood.payment.domain.port.PaymentTransactionRepositoryPort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentOrchestratorService {

  private static final String DEFAULT_UNAVAILABLE_REASON = "PROCPAG_UNAVAILABLE_OR_TIMEOUT";

  private final PaymentTransactionRepositoryPort repository;
  private final PaymentProcessorPort paymentProcessor;
  private final PaymentEventPublisherPort publisher;
  private final PaymentResilienceExecutor resilienceExecutor;

  public PaymentOrchestratorService(
      PaymentTransactionRepositoryPort repository,
      PaymentProcessorPort paymentProcessor,
      PaymentEventPublisherPort publisher,
      PaymentResilienceExecutor resilienceExecutor
  ) {
    this.repository = repository;
    this.paymentProcessor = paymentProcessor;
    this.publisher = publisher;
    this.resilienceExecutor = resilienceExecutor;
  }

  public void processPayment(PaymentTransaction paymentTransaction) {
    log.info("I=Orquestracao de pagamento iniciada para pedido {}", paymentTransaction.orderUuid());
    try {
      var result = resilienceExecutor.execute(
          () -> paymentProcessor.process(
              paymentTransaction.orderUuid(),
              paymentTransaction.customerUuid(),
              paymentTransaction.amount()
          )
      );

      if (result.approved()) {
        log.info("I=Pagamento aprovado pela Procpag para pedido {}", paymentTransaction.orderUuid());
        publishApproved(repository.save(paymentTransaction.withStatus(PaymentStatus.APPROVED)));
        return;
      }

      log.warn("W=Pagamento nao aprovado para pedido {}. Motivo={}", paymentTransaction.orderUuid(), result.reason());
      publishPending(
          repository.save(paymentTransaction.withStatus(PaymentStatus.PENDING)),
          result.reason() == null ? "NOT_APPROVED" : result.reason()
      );
    } catch (Exception ex) {
      log.error("E=Falha ao processar pagamento para pedido {}", paymentTransaction.orderUuid(), ex);
      publishPending(repository.save(paymentTransaction.withStatus(PaymentStatus.PENDING)), DEFAULT_UNAVAILABLE_REASON);
    }
    log.info("I=Orquestracao de pagamento finalizada para pedido {}", paymentTransaction.orderUuid());
  }

  private void publishApproved(PaymentTransaction transaction) {
    publisher.publishApproved(new PaymentApprovedEvent(
        transaction.uuid(),
        transaction.orderUuid(),
        transaction.customerUuid(),
        transaction.amount()
    ));
  }

  private void publishPending(PaymentTransaction transaction, String reason) {
    publisher.publishPending(new PaymentPendingEvent(
        transaction.uuid(),
        transaction.orderUuid(),
        transaction.customerUuid(),
        transaction.amount(),
        reason
    ));
  }
}

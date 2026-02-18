package com.connectfood.payment.application.usecase;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.connectfood.payment.domain.model.PaymentStatus;
import com.connectfood.payment.domain.model.PaymentTransaction;
import com.connectfood.payment.domain.port.PaymentEventPublisherPort;
import com.connectfood.payment.domain.port.PaymentProcessorPort;
import com.connectfood.payment.domain.port.PaymentTransactionRepositoryPort;
import com.connectfood.payment.infrastructure.messaging.events.OrderCreatedEvent;
import com.connectfood.payment.infrastructure.messaging.events.PaymentApprovedEvent;
import com.connectfood.payment.infrastructure.messaging.events.PaymentPendingEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import jakarta.annotation.PreDestroy;

@Service
public class ProcessPaymentOnOrderCreatedUseCase {

  private final PaymentTransactionRepositoryPort repository;
  private final PaymentProcessorPort processor;
  private final PaymentEventPublisherPort publisher;

  private final ExecutorService executor = Executors.newFixedThreadPool(4);
  private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

  private final CircuitBreaker cb;
  private final Retry retry;
  private final TimeLimiter timeLimiter;

  public ProcessPaymentOnOrderCreatedUseCase(
      PaymentTransactionRepositoryPort repository,
      PaymentProcessorPort processor,
      PaymentEventPublisherPort publisher,
      @Value("${resilience.procpag.timeout-ms:1500}") long timeoutMs,
      @Value("${resilience.procpag.retry.max-attempts:3}") int maxAttempts,
      @Value("${resilience.procpag.retry.wait-ms:200}") long waitMs,
      @Value("${resilience.procpag.circuit-breaker.sliding-window-size:10}") int windowSize,
      @Value("${resilience.procpag.circuit-breaker.failure-rate-threshold:50}") float failureRate,
      @Value("${resilience.procpag.circuit-breaker.wait-duration-open-ms:5000}") long openMs,
      @Value("${resilience.procpag.circuit-breaker.permitted-calls-half-open:3}") int halfOpenCalls
  ) {
    this.repository = repository;
    this.processor = processor;
    this.publisher = publisher;

    this.cb = CircuitBreaker.of("procpag", CircuitBreakerConfig.custom()
        .slidingWindowSize(windowSize)
        .failureRateThreshold(failureRate)
        .waitDurationInOpenState(Duration.ofMillis(openMs))
        .permittedNumberOfCallsInHalfOpenState(halfOpenCalls)
        .build()
    );

    this.retry = Retry.of("procpag", RetryConfig.custom()
        .maxAttempts(maxAttempts)
        .waitDuration(Duration.ofMillis(waitMs))
        .build()
    );

    this.timeLimiter = TimeLimiter.of(TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofMillis(timeoutMs))
        .cancelRunningFuture(true)
        .build());
  }

  public void execute(OrderCreatedEvent event) {
    if (repository.findByOrderUuid(event.orderUuid())
        .isPresent()) {
      return;
    }

    var tx = repository.save(new PaymentTransaction(
        null,
        UUID.randomUUID(),
        event.orderUuid(),
        event.customerUuid(),
        PaymentStatus.PENDING,
        event.totalAmount()
    ));

    java.util.function.Supplier<PaymentProcessorPort.PaymentProcessorResult> baseCall =
        () -> processor.process(event.orderUuid(), event.customerUuid(), event.totalAmount());

    var cbSupplier = CircuitBreaker.decorateSupplier(cb, baseCall);
    var retrySupplier = Retry.decorateSupplier(retry, cbSupplier);

    CompletionStage<PaymentProcessorPort.PaymentProcessorResult> stage =
        timeLimiter.executeCompletionStage(
            scheduler,
            () -> CompletableFuture.supplyAsync(retrySupplier, executor)
        );

    stage.whenComplete((result, ex) -> {
      if (ex != null) {
        // fallback obrigatório do professor
        publisher.publishPending(new PaymentPendingEvent(
            tx.uuid(),
            event.orderUuid(),
            event.customerUuid(),
            event.totalAmount(),
            "PROCPAG_UNAVAILABLE_OR_TIMEOUT"
        ));
        return;
      }

      if (result != null && result.approved()) {
        publisher.publishApproved(new PaymentApprovedEvent(
            tx.uuid(),
            event.orderUuid(),
            event.customerUuid(),
            event.totalAmount()
        ));
      } else {
        publisher.publishPending(new PaymentPendingEvent(
            tx.uuid(),
            event.orderUuid(),
            event.customerUuid(),
            event.totalAmount(),
            "NOT_APPROVED"
        ));
      }
    });
  }

  @PreDestroy
  public void shutdownExecutors() {
    executor.shutdown();
    scheduler.shutdown();
  }
}

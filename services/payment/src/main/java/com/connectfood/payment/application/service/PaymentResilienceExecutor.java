package com.connectfood.payment.application.service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import jakarta.annotation.PreDestroy;

@Component
public class PaymentResilienceExecutor {

  private final ExecutorService executor = Executors.newFixedThreadPool(4);
  private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);

  private final CircuitBreaker circuitBreaker;
  private final Retry retry;
  private final TimeLimiter timeLimiter;

  public PaymentResilienceExecutor(
      @Value("${resilience.procpag.timeout-ms:1500}") long timeoutMs,
      @Value("${resilience.procpag.retry.max-attempts:3}") int maxAttempts,
      @Value("${resilience.procpag.retry.wait-ms:200}") long waitMs,
      @Value("${resilience.procpag.circuit-breaker.sliding-window-size:10}") int windowSize,
      @Value("${resilience.procpag.circuit-breaker.failure-rate-threshold:50}") float failureRate,
      @Value("${resilience.procpag.circuit-breaker.wait-duration-open-ms:5000}") long openMs,
      @Value("${resilience.procpag.circuit-breaker.permitted-calls-half-open:3}") int halfOpenCalls
  ) {
    this.circuitBreaker = CircuitBreaker.of("procpag", CircuitBreakerConfig.custom()
        .slidingWindowSize(windowSize)
        .failureRateThreshold(failureRate)
        .waitDurationInOpenState(Duration.ofMillis(openMs))
        .permittedNumberOfCallsInHalfOpenState(halfOpenCalls)
        .build());

    this.retry = Retry.of("procpag", RetryConfig.custom()
        .maxAttempts(maxAttempts)
        .waitDuration(Duration.ofMillis(waitMs))
        .build());

    this.timeLimiter = TimeLimiter.of(TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofMillis(timeoutMs))
        .cancelRunningFuture(true)
        .build());
  }

  public <T> T execute(Supplier<T> supplier) {
    var circuitBreakerSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
    var retrySupplier = Retry.decorateSupplier(retry, circuitBreakerSupplier);

    CompletionStage<T> stage = timeLimiter.executeCompletionStage(
        scheduler,
        () -> CompletableFuture.supplyAsync(retrySupplier, executor)
    );

    try {
      return stage.toCompletableFuture().join();
    } catch (CompletionException ex) {
      throw new IllegalStateException("Procpag risk API call failed", ex.getCause() == null ? ex : ex.getCause());
    }
  }

  @PreDestroy
  public void shutdownExecutors() {
    executor.shutdown();
    scheduler.shutdown();
  }
}

package com.connectfood.payment.infrastructure.messaging;

import com.connectfood.payment.application.command.PendingPaymentCommand;
import com.connectfood.payment.application.service.PaymentResilienceExecutor;
import com.connectfood.payment.application.usecase.ReprocessPendingPaymentUseCase;
import com.connectfood.payment.domain.event.PaymentPendingEvent;
import com.connectfood.payment.domain.model.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentPendingWorker {

  private static final String REPROCESS_ATTEMPT_HEADER = "x-pending-reprocess-attempt";

  private final ObjectMapper mapper;
  private final ReprocessPendingPaymentUseCase useCase;
  private final PaymentResilienceExecutor resilienceExecutor;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final String pendingTopic;
  private final int maxReprocessAttempts;

  public PaymentPendingWorker(
      ObjectMapper mapper,
      ReprocessPendingPaymentUseCase useCase,
      PaymentResilienceExecutor resilienceExecutor,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${payment.topics.payment-pending:pagamento.pendente}") String pendingTopic,
      @Value("${payment.pending-reprocess.max-attempts:8}") int maxReprocessAttempts
  ) {
    this.mapper = mapper;
    this.useCase = useCase;
    this.resilienceExecutor = resilienceExecutor;
    this.kafkaTemplate = kafkaTemplate;
    this.pendingTopic = pendingTopic;
    this.maxReprocessAttempts = maxReprocessAttempts;
  }

  @KafkaListener(topics = "${payment.topics.payment-pending:pagamento.pendente}", groupId = "payment-worker")
  public void onPending(ConsumerRecord<String, String> record) {
    var payload = record.value();
    var attempt = readAttempt(record);
    try {
      log.info("I=Evento pagamento.pendente recebido para reprocessamento. tentativa={}", attempt);
      var event = mapper.readValue(payload, PaymentPendingEvent.class);
      if (attempt >= maxReprocessAttempts) {
        log.error(
            "E=Limite maximo de tentativas atingido para pedido {}. Mensagem finalizada sem novo reenvio",
            event.orderUuid()
        );
        return;
      }

      if (resilienceExecutor.isCircuitOpen()) {
        log.warn(
            "W=Circuit breaker aberto. Reencaminhando pedido {} para o final da fila. tentativa={}",
            event.orderUuid(),
            attempt
        );
        requeueToPending(event, attempt + 1);
        return;
      }

      log.info("I=Iniciando reprocessamento do pagamento do pedido {}", event.orderUuid());
      var status = useCase.execute(new PendingPaymentCommand(
          event.paymentUuid(),
          event.orderUuid(),
          event.customerUuid(),
          event.amount(),
          event.reason()
      ));
      if (status != PaymentStatus.APPROVED) {
        log.warn(
            "W=Pagamento do pedido {} continua pendente. Reencaminhando para o final da fila. tentativa={}",
            event.orderUuid(),
            attempt
        );
        requeueToPending(event, attempt + 1);
        return;
      }
      log.info("I=Reprocessamento de pagamento finalizado para pedido {}", event.orderUuid());
    } catch (Exception ex) {
      log.error(
          "E=Falha ao processar evento pagamento.pendente tentativa={} mensagem={} payload={}",
          attempt,
          ex.getMessage(),
          payload
      );
    }
  }

  private int readAttempt(ConsumerRecord<String, String> record) {
    var header = record.headers().lastHeader(REPROCESS_ATTEMPT_HEADER);
    if (header == null) {
      return 0;
    }
    try {
      return Integer.parseInt(new String(header.value(), StandardCharsets.UTF_8));
    } catch (Exception ex) {
      log.warn("W=Header de tentativa invalido. Assumindo tentativa 0");
      return 0;
    }
  }

  private void requeueToPending(PaymentPendingEvent event, int nextAttempt) {
    try {
      var message = MessageBuilder.withPayload(mapper.writeValueAsString(event))
          .setHeader(KafkaHeaders.TOPIC, pendingTopic)
          .setHeader(KafkaHeaders.KEY, event.orderUuid().toString())
          .setHeader(REPROCESS_ATTEMPT_HEADER, Integer.toString(nextAttempt))
          .build();
      kafkaTemplate.send(message);
      log.info(
          "I=Pedido {} reenviado para pagamento.pendente no final da fila. proxima_tentativa={}",
          event.orderUuid(),
          nextAttempt
      );
    } catch (Exception ex) {
      log.error("E=Falha ao reenviar pedido {} para pagamento.pendente mensagem={}", event.orderUuid(), ex.getMessage());
    }
  }
}

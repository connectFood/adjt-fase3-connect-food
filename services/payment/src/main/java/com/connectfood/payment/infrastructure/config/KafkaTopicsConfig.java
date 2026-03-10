package com.connectfood.payment.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

  @Bean
  public NewTopic pedidoCriado(@Value("${payment.topics.order-created:pedido.criado}") String name) {
    return TopicBuilder.name(name)
        .partitions(1)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic pagamentoAprovado(@Value("${payment.topics.payment-approved:pagamento.aprovado}") String name) {
    return TopicBuilder.name(name)
        .partitions(1)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic pagamentoPendente(@Value("${payment.topics.payment-pending:pagamento.pendente}") String name) {
    return TopicBuilder.name(name)
        .partitions(1)
        .replicas(1)
        .build();
  }
}

package com.connectfood.order.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

  @Bean
  public NewTopic orderCreatedTopic(@Value("${order.topics.order-created}") String topicName) {
    return TopicBuilder.name(topicName)
        .partitions(1)
        .replicas(1)
        .build();
  }
}

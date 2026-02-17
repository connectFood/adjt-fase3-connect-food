package com.connectfood.order.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.port.OrderRepositoryPort;
import com.connectfood.order.infrastructure.persistence.mapper.OrderInfraMapper;
import com.connectfood.order.infrastructure.persistence.repository.JpaOrderRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

  private final JpaOrderRepository jpa;

  public OrderRepositoryAdapter(JpaOrderRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional
  public Order save(Order order) {
    var saved = jpa.save(OrderInfraMapper.toEntity(order));
    return OrderInfraMapper.toDomain(saved);
  }

  @Override
  public Optional<Order> findByUuid(UUID uuid) {
    return jpa.findByUuid(uuid)
        .map(OrderInfraMapper::toDomain);
  }

  @Override
  public List<Order> findByCustomerUuid(UUID customerUuid) {
    return jpa.findByCustomerUuid(customerUuid)
        .stream()
        .map(OrderInfraMapper::toDomain)
        .toList();
  }
}

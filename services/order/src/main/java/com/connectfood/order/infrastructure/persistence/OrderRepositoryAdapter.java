package com.connectfood.order.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.order.domain.model.Order;
import com.connectfood.order.domain.model.OrderStatus;
import com.connectfood.order.domain.port.OrderRepositoryPort;
import com.connectfood.order.infrastructure.persistence.mapper.OrderInfraMapper;
import com.connectfood.order.infrastructure.persistence.repository.JpaOrderRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

  private final JpaOrderRepository jpa;

  public OrderRepositoryAdapter(JpaOrderRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  @Transactional
  public Order save(Order order) {
    log.info("I=Persistindo pedido customerUuid={} restaurantId={} status={}", order.customerUuid(), order.restaurantId(),
        order.status()
    );
    var saved = jpa.save(OrderInfraMapper.toEntity(order));
    log.info("I=Pedido persistido com sucesso uuid={}", saved.getUuid());
    return OrderInfraMapper.toDomain(saved);
  }

  @Override
  public Optional<Order> findByUuid(UUID uuid) {
    log.info("I=Consultando pedido no banco por uuid={}", uuid);
    return jpa.findByUuid(uuid)
        .map(OrderInfraMapper::toDomain);
  }

  @Override
  public List<Order> findByCustomerUuid(UUID customerUuid) {
    log.info("I=Consultando pedidos no banco por customerUuid={}", customerUuid);
    return jpa.findByCustomerUuid(customerUuid)
        .stream()
        .map(OrderInfraMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public boolean updateStatusByUuid(UUID orderUuid, OrderStatus status) {
    log.info("I=Atualizando status do pedido no banco orderUuid={} novoStatus={}", orderUuid, status);
    return jpa.updateStatusByUuid(orderUuid, status.name()) > 0;
  }
}

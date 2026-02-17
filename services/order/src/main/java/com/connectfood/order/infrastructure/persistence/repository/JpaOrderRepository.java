package com.connectfood.order.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.order.infrastructure.persistence.entity.OrderEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {
  Optional<OrderEntity> findByUuid(UUID uuid);

  List<OrderEntity> findByCustomerUuid(UUID customerUuid);
}

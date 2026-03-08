package com.connectfood.order.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.connectfood.order.infrastructure.persistence.entity.OrderEntity;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {
  Optional<OrderEntity> findByUuid(UUID uuid);

  List<OrderEntity> findByCustomerUuid(UUID customerUuid);

  @Modifying
  @Query("update OrderEntity o set o.status = :status, o.updatedAt = CURRENT_TIMESTAMP where o.uuid = :uuid")
  int updateStatusByUuid(@Param("uuid") UUID uuid, @Param("status") String status);
}

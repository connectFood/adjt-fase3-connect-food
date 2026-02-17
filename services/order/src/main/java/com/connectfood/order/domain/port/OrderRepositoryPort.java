package com.connectfood.order.domain.port;

import com.connectfood.order.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {
  Order save(Order order);

  Optional<Order> findByUuid(UUID uuid);

  List<Order> findByCustomerUuid(UUID customerUuid);
}

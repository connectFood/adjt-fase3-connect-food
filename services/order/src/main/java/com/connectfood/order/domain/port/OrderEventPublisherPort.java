package com.connectfood.order.domain.port;

import com.connectfood.order.domain.model.Order;

public interface OrderEventPublisherPort {
  void publishOrderCreated(Order order);
}

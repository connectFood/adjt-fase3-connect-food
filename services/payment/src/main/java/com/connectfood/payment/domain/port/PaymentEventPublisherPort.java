package com.connectfood.payment.domain.port;

import com.connectfood.payment.infrastructure.messaging.events.PaymentApprovedEvent;
import com.connectfood.payment.infrastructure.messaging.events.PaymentPendingEvent;

public interface PaymentEventPublisherPort {
  void publishApproved(PaymentApprovedEvent event);

  void publishPending(PaymentPendingEvent event);
}

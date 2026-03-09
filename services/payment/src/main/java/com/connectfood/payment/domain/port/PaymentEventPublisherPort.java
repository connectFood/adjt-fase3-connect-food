package com.connectfood.payment.domain.port;

import com.connectfood.payment.domain.event.PaymentApprovedEvent;
import com.connectfood.payment.domain.event.PaymentPendingEvent;

public interface PaymentEventPublisherPort {
  void publishApproved(PaymentApprovedEvent event);

  void publishPending(PaymentPendingEvent event);
}

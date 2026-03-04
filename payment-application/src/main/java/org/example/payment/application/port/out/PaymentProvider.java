package org.example.payment.application.port.out;

import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentStatus;

public interface PaymentProvider {

    /**
     * Authorize the given payment with the external provider.
     *
     * @param payment the payment to authorize
     * @return AUTHORIZED if approved, DECLINED if declined
     */
    PaymentStatus authorize(Payment payment);
}


package org.example.payment.application.port.out;

import org.example.payment.domain.model.Payment;

public interface PaymentEvents {

    void publishAuthorized(Payment payment);

    void publishDeclined(Payment payment);
}


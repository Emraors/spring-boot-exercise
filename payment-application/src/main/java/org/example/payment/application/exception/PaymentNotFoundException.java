package org.example.payment.application.exception;

import org.example.payment.domain.exception.DomainException;
import org.example.payment.domain.model.PaymentId;

public class PaymentNotFoundException extends DomainException {

    public PaymentNotFoundException(PaymentId id) {
        super("Payment not found with id: " + id);
    }
}


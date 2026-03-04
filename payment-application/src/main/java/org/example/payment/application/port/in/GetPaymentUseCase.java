package org.example.payment.application.port.in;

import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.springframework.cache.annotation.Cacheable;

public interface GetPaymentUseCase {

    @Cacheable(value = "payments", key = "#root.args[0].value()")
    Payment getById(PaymentId id);
}


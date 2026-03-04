package org.example.payment.application.port.out;

import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    Optional<PaymentId> findByIdempotencyKey(String idempotencyKey);

    void saveIdempotencyKey(String idempotencyKey, PaymentId paymentId);
}


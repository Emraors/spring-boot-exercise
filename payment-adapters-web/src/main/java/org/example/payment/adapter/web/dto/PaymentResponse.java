package org.example.payment.adapter.web.dto;

import org.example.payment.domain.model.Payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        long cents,
        String currency,
        String status,
        Instant createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId().value(),
                payment.getAmount().cents(),
                payment.getAmount().currency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}


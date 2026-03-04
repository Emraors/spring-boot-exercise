package org.example.payment.domain.model;

import java.util.UUID;

public record PaymentId(UUID value) {

    public static PaymentId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("PaymentId value must not be null");
        }
        return new PaymentId(value);
    }

    public static PaymentId newId() {
        return new PaymentId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}


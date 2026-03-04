package org.example.payment.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
        @Positive(message = "cents must be positive")
        long cents,

        @NotBlank(message = "currency must not be blank")
        String currency,

        @NotBlank(message = "idempotencyKey must not be blank")
        String idempotencyKey
) {}


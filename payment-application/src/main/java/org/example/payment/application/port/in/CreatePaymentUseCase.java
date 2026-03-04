package org.example.payment.application.port.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.example.payment.domain.model.Payment;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CreatePaymentUseCase {

    Payment create(@Valid CreatePaymentCommand command);

    record CreatePaymentCommand(
            @Positive long cents,
            @NotBlank String currency,
            @NotBlank String idempotencyKey
    ) {}
}


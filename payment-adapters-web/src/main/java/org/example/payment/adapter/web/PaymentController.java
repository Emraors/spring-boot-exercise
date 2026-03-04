package org.example.payment.adapter.web;

import jakarta.validation.Valid;
import org.example.payment.adapter.web.dto.CreatePaymentRequest;
import org.example.payment.adapter.web.dto.PaymentResponse;
import org.example.payment.application.port.in.CreatePaymentUseCase;
import org.example.payment.application.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import org.example.payment.application.port.in.GetPaymentUseCase;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                             GetPaymentUseCase getPaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        CreatePaymentCommand command = new CreatePaymentCommand(
                request.cents(),
                request.currency(),
                request.idempotencyKey()
        );
        Payment payment = createPaymentUseCase.create(command);
        return PaymentResponse.from(payment);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        Payment payment = getPaymentUseCase.getById(PaymentId.of(id));
        return PaymentResponse.from(payment);
    }
}


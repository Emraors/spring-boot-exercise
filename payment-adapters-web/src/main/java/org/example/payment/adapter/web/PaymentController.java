package org.example.payment.adapter.web;

import org.example.payment.adapter.web.api.PaymentsApi;
import org.example.payment.adapter.web.api.model.CreatePaymentRequest;
import org.example.payment.adapter.web.api.model.PaymentResponse;
import org.example.payment.application.port.in.CreatePaymentUseCase;
import org.example.payment.application.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import org.example.payment.application.port.in.GetPaymentUseCase;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
public class PaymentController implements PaymentsApi {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final GetPaymentUseCase getPaymentUseCase;

    public PaymentController(CreatePaymentUseCase createPaymentUseCase,
                             GetPaymentUseCase getPaymentUseCase) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.getPaymentUseCase = getPaymentUseCase;
    }

    @Override
    public ResponseEntity<PaymentResponse> createPayment(CreatePaymentRequest request) {
        CreatePaymentCommand command = new CreatePaymentCommand(
                request.getCents(),
                request.getCurrency(),
                request.getIdempotencyKey()
        );
        Payment payment = createPaymentUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(payment));
    }

    @Override
    public ResponseEntity<PaymentResponse> getPaymentById(UUID id) {
        Payment payment = getPaymentUseCase.getById(PaymentId.of(id));
        return ResponseEntity.ok(toResponse(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId().value());
        response.setCents(payment.getAmount().cents());
        response.setCurrency(payment.getAmount().currency());
        response.setStatus(PaymentResponse.StatusEnum.fromValue(payment.getStatus().name()));
        response.setCreatedAt(OffsetDateTime.ofInstant(payment.getCreatedAt(), ZoneOffset.UTC));
        return response;
    }
}


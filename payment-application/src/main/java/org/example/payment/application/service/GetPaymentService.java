package org.example.payment.application.service;

import org.example.payment.application.exception.PaymentNotFoundException;
import org.example.payment.application.port.in.GetPaymentUseCase;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;

public class GetPaymentService implements GetPaymentUseCase {

    private final PaymentRepository paymentRepository;

    public GetPaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment getById(PaymentId id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }
}


package org.example.payment.application.service;

import org.example.payment.application.exception.PaymentNotFoundException;
import org.example.payment.application.port.in.CreatePaymentUseCase;
import org.example.payment.application.port.out.PaymentEvents;
import org.example.payment.application.port.out.PaymentProvider;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.springframework.transaction.annotation.Transactional;

public class CreatePaymentService implements CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final PaymentEvents paymentEvents;

    public CreatePaymentService(PaymentRepository paymentRepository,
                                PaymentProvider paymentProvider,
                                PaymentEvents paymentEvents) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
        this.paymentEvents = paymentEvents;
    }

    @Override
    @Transactional
    public Payment create(CreatePaymentCommand command) {
        // Idempotency check: if key already used, return existing payment
        return paymentRepository.findByIdempotencyKey(command.idempotencyKey())
                .flatMap(paymentRepository::findById)
                .orElseGet(() -> createNewPayment(command));
    }

    private Payment createNewPayment(CreatePaymentCommand command) {
        Money amount = new Money(command.cents(), command.currency());
        Payment payment = Payment.create(amount);

        paymentRepository.save(payment);
        paymentRepository.saveIdempotencyKey(command.idempotencyKey(), payment.getId());

        PaymentStatus providerStatus = paymentProvider.authorize(payment);

        if (providerStatus == PaymentStatus.AUTHORIZED) {
            payment.authorize();
            paymentRepository.save(payment);
            paymentEvents.publishAuthorized(payment);
        } else {
            payment.decline();
            paymentRepository.save(payment);
            paymentEvents.publishDeclined(payment);
        }

        return payment;
    }
}


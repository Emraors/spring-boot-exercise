package org.example.payment.app.config;

import org.example.payment.application.port.in.CreatePaymentUseCase;
import org.example.payment.application.port.in.GetPaymentUseCase;
import org.example.payment.application.port.out.PaymentEvents;
import org.example.payment.application.port.out.PaymentProvider;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.application.service.CreatePaymentService;
import org.example.payment.application.service.GetPaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreatePaymentUseCase createPaymentUseCase(PaymentRepository paymentRepository,
                                                      PaymentProvider paymentProvider,
                                                      PaymentEvents paymentEvents) {
        return new CreatePaymentService(paymentRepository, paymentProvider, paymentEvents);
    }

    @Bean
    public GetPaymentUseCase getPaymentUseCase(PaymentRepository paymentRepository) {
        return new GetPaymentService(paymentRepository);
    }
}


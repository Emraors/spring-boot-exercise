package org.example.payment.application.context;

import jakarta.validation.ConstraintViolationException;
import org.example.payment.application.port.in.CreatePaymentUseCase;
import org.example.payment.application.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import org.example.payment.application.port.out.PaymentEvents;
import org.example.payment.application.port.out.PaymentProvider;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.application.service.CreatePaymentService;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Minimal-context test verifying that @Validated on CreatePaymentUseCase triggers
 * Bean Validation when called through a Spring proxy.
 * Only loads ValidationAutoConfiguration + the service bean — no full Spring Boot context.
 */
@SpringJUnitConfig
@DisplayName("CreatePaymentUseCase @Validated minimal context test")
class CreatePaymentValidationContextTest {

    @Configuration
    @Import(ValidationAutoConfiguration.class)
    static class TestConfig {

        @Bean
        PaymentRepository paymentRepository() {
            PaymentRepository repo = mock(PaymentRepository.class);
            when(repo.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
            return repo;
        }

        @Bean
        PaymentProvider paymentProvider() {
            PaymentProvider provider = mock(PaymentProvider.class);
            when(provider.authorize(any())).thenReturn(PaymentStatus.AUTHORIZED);
            return provider;
        }

        @Bean
        PaymentEvents paymentEvents() {
            return mock(PaymentEvents.class);
        }

        @Bean
        CreatePaymentUseCase createPaymentUseCase(PaymentRepository repo, PaymentProvider provider, PaymentEvents events) {
            return new CreatePaymentService(repo, provider, events);
        }
    }

    @Autowired
    private CreatePaymentUseCase createPaymentUseCase;

    @Test
    @DisplayName("command with negative cents fails @Positive validation")
    void create_negativeCents_throwsConstraintViolationException() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(-1L, "USD", "key-val");

        assertThatThrownBy(() -> createPaymentUseCase.create(cmd))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("command with blank currency fails @NotBlank validation")
    void create_blankCurrency_throwsConstraintViolationException() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(100L, "", "key-val2");

        assertThatThrownBy(() -> createPaymentUseCase.create(cmd))
                .isInstanceOf(ConstraintViolationException.class);
    }
}


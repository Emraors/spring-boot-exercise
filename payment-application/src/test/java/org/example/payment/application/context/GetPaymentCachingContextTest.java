package org.example.payment.application.context;

import org.example.payment.application.port.in.GetPaymentUseCase;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.application.service.GetPaymentService;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Minimal-context test verifying that @Cacheable on GetPaymentUseCase works
 * when the service is wrapped in a Spring caching proxy.
 * Only loads CacheManager + the service — no full Spring Boot context.
 */
@SpringJUnitConfig
@DisplayName("GetPaymentUseCase @Cacheable minimal context test")
class GetPaymentCachingContextTest {

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        org.springframework.cache.CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("payments");
        }

        @Bean
        PaymentRepository paymentRepository() {
            return mock(PaymentRepository.class);
        }

        @Bean
        GetPaymentUseCase getPaymentUseCase(PaymentRepository repo) {
            return new GetPaymentService(repo);
        }
    }

    @Autowired
    private GetPaymentUseCase getPaymentUseCase;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("second call with same id hits cache — repository called only once")
    void getById_calledTwice_repositoryInvokedOnce() {
        PaymentId id = PaymentId.newId();
        Payment payment = Payment.reconstitute(id, new Money(1000L, "USD"), PaymentStatus.AUTHORIZED, Instant.now());
        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        Payment first = getPaymentUseCase.getById(id);
        Payment second = getPaymentUseCase.getById(id);

        assertThat(first).isSameAs(second);
        verify(paymentRepository, times(1)).findById(id);
    }
}


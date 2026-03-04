package org.example.payment.application.service;

import org.example.payment.application.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import org.example.payment.application.port.out.PaymentEvents;
import org.example.payment.application.port.out.PaymentProvider;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePaymentService")
class CreatePaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentProvider paymentProvider;
    @Mock
    private PaymentEvents paymentEvents;

    @InjectMocks
    private CreatePaymentService service;

    @BeforeEach
    void setUp() {
        // no-op: @InjectMocks handles construction
    }

    @Test
    @DisplayName("creates payment, calls provider, authorizes and publishes event on AUTHORIZED")
    void create_providerAuthorizes_statusAuthorizedAndEventPublished() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(1000L, "USD", "key-1");

        when(paymentRepository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProvider.authorize(any())).thenReturn(PaymentStatus.AUTHORIZED);

        Payment result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentEvents).publishAuthorized(result);
        verify(paymentEvents, never()).publishDeclined(any());
    }

    @Test
    @DisplayName("creates payment, calls provider, declines and publishes declined event")
    void create_providerDeclines_statusDeclinedAndEventPublished() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(1000L, "USD", "key-2");

        when(paymentRepository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProvider.authorize(any())).thenReturn(PaymentStatus.DECLINED);

        Payment result = service.create(cmd);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.DECLINED);
        verify(paymentEvents).publishDeclined(result);
        verify(paymentEvents, never()).publishAuthorized(any());
    }

    @Test
    @DisplayName("idempotency: same key returns existing payment without calling provider again")
    void create_idempotencyKeyExists_returnsExistingPayment() {
        PaymentId existingId = PaymentId.newId();
        Payment existing = Payment.reconstitute(existingId,
                new org.example.payment.domain.model.Money(1000L, "USD"),
                PaymentStatus.AUTHORIZED,
                java.time.Instant.now());

        when(paymentRepository.findByIdempotencyKey("key-dup")).thenReturn(Optional.of(existingId));
        when(paymentRepository.findById(existingId)).thenReturn(Optional.of(existing));

        CreatePaymentCommand cmd = new CreatePaymentCommand(1000L, "USD", "key-dup");
        Payment result = service.create(cmd);

        assertThat(result.getId()).isEqualTo(existingId);
        verify(paymentProvider, never()).authorize(any());
        verify(paymentEvents, never()).publishAuthorized(any());
    }

    @Test
    @DisplayName("saves idempotency key on new payment creation")
    void create_newPayment_savesIdempotencyKey() {
        CreatePaymentCommand cmd = new CreatePaymentCommand(500L, "EUR", "key-new");

        when(paymentRepository.findByIdempotencyKey("key-new")).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentProvider.authorize(any())).thenReturn(PaymentStatus.AUTHORIZED);

        service.create(cmd);

        verify(paymentRepository).saveIdempotencyKey(eq("key-new"), any(PaymentId.class));
    }
}


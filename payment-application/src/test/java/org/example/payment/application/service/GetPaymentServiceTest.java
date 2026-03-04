package org.example.payment.application.service;

import org.example.payment.application.exception.PaymentNotFoundException;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPaymentService")
class GetPaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetPaymentService service;

    @Test
    @DisplayName("getById returns payment when found")
    void getById_found_returnsPayment() {
        PaymentId id = PaymentId.newId();
        Payment payment = Payment.reconstitute(id, new Money(1000L, "USD"), PaymentStatus.AUTHORIZED, Instant.now());

        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        Payment result = service.getById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("getById throws PaymentNotFoundException when not found")
    void getById_notFound_throwsException() {
        PaymentId id = PaymentId.newId();

        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(id.toString());
    }
}


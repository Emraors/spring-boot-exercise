package org.example.payment.adapter.persistence;

import org.example.payment.adapter.persistence.mapper.PaymentPersistenceMapper;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({PaymentRepositoryAdapter.class, PaymentPersistenceMapper.class})
@DisplayName("PaymentRepositoryAdapter @DataJpaTest")
class PaymentRepositoryAdapterTest {

    @Autowired
    private PaymentRepositoryAdapter repositoryAdapter;

    @Test
    @DisplayName("save and findById round-trip returns correct payment")
    void saveAndFindById_roundTrip() {
        Payment payment = Payment.create(new Money(1500L, "USD"));

        repositoryAdapter.save(payment);

        Optional<Payment> found = repositoryAdapter.findById(payment.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(payment.getId());
        assertThat(found.get().getAmount().cents()).isEqualTo(1500L);
        assertThat(found.get().getAmount().currency()).isEqualTo("USD");
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    @DisplayName("findById returns empty for unknown id")
    void findById_unknownId_returnsEmpty() {
        Optional<Payment> found = repositoryAdapter.findById(PaymentId.newId());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("save updates payment status correctly")
    void save_updatesStatus() {
        Payment payment = Payment.create(new Money(2000L, "EUR"));
        repositoryAdapter.save(payment);

        payment.authorize();
        repositoryAdapter.save(payment);

        Optional<Payment> found = repositoryAdapter.findById(payment.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("saveIdempotencyKey and findByIdempotencyKey round-trip")
    void idempotencyKey_saveAndFind() {
        Payment payment = Payment.create(new Money(500L, "GBP"));
        repositoryAdapter.save(payment);
        repositoryAdapter.saveIdempotencyKey("my-key", payment.getId());

        Optional<PaymentId> found = repositoryAdapter.findByIdempotencyKey("my-key");
        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(payment.getId());
    }

    @Test
    @DisplayName("duplicate idempotency key throws DataIntegrityViolationException")
    void idempotencyKey_duplicate_throws() {
        Payment p1 = Payment.create(new Money(100L, "USD"));
        Payment p2 = Payment.create(new Money(200L, "USD"));
        repositoryAdapter.save(p1);
        repositoryAdapter.save(p2);
        repositoryAdapter.saveIdempotencyKey("dup-key3", p1.getId());

        assertThatThrownBy(() -> repositoryAdapter.saveIdempotencyKey("dup-key3", p2.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}


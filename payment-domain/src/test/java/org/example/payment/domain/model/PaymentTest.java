package org.example.payment.domain.model;

import org.example.payment.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment domain aggregate")
class PaymentTest {

    @Test
    @DisplayName("create() with positive amount produces CREATED status")
    void create_withPositiveAmount_isCreated() {
        Money amount = new Money(1000L, "USD");
        Payment payment = Payment.create(amount);

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create() with zero amount throws DomainException")
    void create_withZeroAmount_throwsDomainException() {
        Money amount = new Money(0L, "USD");

        assertThatThrownBy(() -> Payment.create(amount))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("create() with negative amount throws DomainException")
    void create_withNegativeAmount_throwsDomainException() {
        Money amount = new Money(-500L, "USD");

        assertThatThrownBy(() -> Payment.create(amount))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("authorize() transitions status to AUTHORIZED")
    void authorize_changesStatusToAuthorized() {
        Payment payment = Payment.create(new Money(1000L, "EUR"));

        payment.authorize();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("decline() transitions status to DECLINED")
    void decline_changesStatusToDeclined() {
        Payment payment = Payment.create(new Money(1000L, "EUR"));

        payment.decline();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    }

    @Test
    @DisplayName("Money with blank currency throws IllegalArgumentException")
    void money_withBlankCurrency_throws() {
        assertThatThrownBy(() -> new Money(100L, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Money.isPositive() is false for zero cents")
    void money_isPositive_falseForZero() {
        assertThat(new Money(0L, "USD").isPositive()).isFalse();
    }

    @Test
    @DisplayName("Money.add() sums same-currency amounts")
    void money_add_sameCurrency() {
        Money a = new Money(500L, "USD");
        Money b = new Money(300L, "USD");

        assertThat(a.add(b)).isEqualTo(new Money(800L, "USD"));
    }

    @Test
    @DisplayName("Money.add() throws for different currencies")
    void money_add_differentCurrencies_throws() {
        Money a = new Money(500L, "USD");
        Money b = new Money(300L, "EUR");

        assertThatThrownBy(() -> a.add(b))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currencies");
    }

    @Test
    @DisplayName("PaymentId.of() with null throws IllegalArgumentException")
    void paymentId_ofNull_throws() {
        assertThatThrownBy(() -> PaymentId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}


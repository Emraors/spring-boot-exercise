package org.example.payment.domain.model;

public record Money(long cents, String currency) {

    public Money {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
    }

    public boolean isPositive() {
        return cents > 0;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot add money with different currencies: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.cents + other.cents, this.currency);
    }

    @Override
    public String toString() {
        return cents + " " + currency;
    }
}


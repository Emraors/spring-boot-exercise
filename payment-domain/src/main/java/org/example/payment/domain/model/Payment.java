package org.example.payment.domain.model;

import org.example.payment.domain.exception.DomainException;

import java.time.Instant;

public class Payment {

    private final PaymentId id;
    private final Money amount;
    private PaymentStatus status;
    private final Instant createdAt;

    private Payment(PaymentId id, Money amount, PaymentStatus status, Instant createdAt) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Factory method to create a new Payment in CREATED state.
     * Enforces domain invariant: amount must be positive.
     */
    public static Payment create(Money amount) {
        if (!amount.isPositive()) {
            throw new DomainException("Payment amount must be positive, got: " + amount.cents());
        }
        return new Payment(PaymentId.newId(), amount, PaymentStatus.CREATED, Instant.now());
    }

    /**
     * Reconstitute a Payment from persistence (no invariant re-check).
     */
    public static Payment reconstitute(PaymentId id, Money amount, PaymentStatus status, Instant createdAt) {
        return new Payment(id, amount, status, createdAt);
    }

    public void authorize() {
        this.status = PaymentStatus.AUTHORIZED;
    }

    public void decline() {
        this.status = PaymentStatus.DECLINED;
    }

    public PaymentId getId() {
        return id;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}


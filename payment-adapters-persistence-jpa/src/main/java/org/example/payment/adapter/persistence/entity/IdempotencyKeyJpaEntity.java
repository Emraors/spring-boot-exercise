package org.example.payment.adapter.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyJpaEntity {

    @Id
    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "payment_id", nullable = false, columnDefinition = "uuid")
    private UUID paymentId;

    protected IdempotencyKeyJpaEntity() {}

    public IdempotencyKeyJpaEntity(String idempotencyKey, UUID paymentId) {
        this.idempotencyKey = idempotencyKey;
        this.paymentId = paymentId;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getPaymentId() { return paymentId; }
}


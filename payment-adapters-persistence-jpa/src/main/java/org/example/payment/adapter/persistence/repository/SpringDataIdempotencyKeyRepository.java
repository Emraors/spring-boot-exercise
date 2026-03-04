package org.example.payment.adapter.persistence.repository;

import org.example.payment.adapter.persistence.entity.IdempotencyKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataIdempotencyKeyRepository extends JpaRepository<IdempotencyKeyJpaEntity, String> {

    /**
     * Native INSERT — throws ConstraintViolationException on duplicate key
     * instead of silently merging (updating) via save().
     */
    @Modifying
    @Query(value = "INSERT INTO idempotency_keys (idempotency_key, payment_id) VALUES (:key, :paymentId)",
           nativeQuery = true)
    void insertIdempotencyKey(@Param("key") String key, @Param("paymentId") UUID paymentId);
}


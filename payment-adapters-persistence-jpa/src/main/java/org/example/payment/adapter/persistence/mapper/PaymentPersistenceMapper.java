package org.example.payment.adapter.persistence.mapper;

import org.example.payment.adapter.persistence.entity.PaymentJpaEntity;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceMapper {

    public PaymentJpaEntity toEntity(Payment payment) {
        return new PaymentJpaEntity(
                payment.getId().value(),
                payment.getAmount().cents(),
                payment.getAmount().currency(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        return Payment.reconstitute(
                PaymentId.of(entity.getId()),
                new Money(entity.getCents(), entity.getCurrency()),
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }
}


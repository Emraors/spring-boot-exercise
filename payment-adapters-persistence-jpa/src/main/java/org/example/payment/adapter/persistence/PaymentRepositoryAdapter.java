package org.example.payment.adapter.persistence;

import org.example.payment.adapter.persistence.entity.IdempotencyKeyJpaEntity;
import org.example.payment.adapter.persistence.entity.PaymentJpaEntity;
import org.example.payment.adapter.persistence.mapper.PaymentPersistenceMapper;
import org.example.payment.adapter.persistence.repository.SpringDataIdempotencyKeyRepository;
import org.example.payment.adapter.persistence.repository.SpringDataPaymentRepository;
import org.example.payment.application.port.out.PaymentRepository;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final SpringDataPaymentRepository paymentRepo;
    private final SpringDataIdempotencyKeyRepository idempotencyRepo;
    private final PaymentPersistenceMapper mapper;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository paymentRepo,
                                    SpringDataIdempotencyKeyRepository idempotencyRepo,
                                    PaymentPersistenceMapper mapper) {
        this.paymentRepo = paymentRepo;
        this.idempotencyRepo = idempotencyRepo;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toEntity(payment);
        PaymentJpaEntity saved = paymentRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return paymentRepo.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<PaymentId> findByIdempotencyKey(String idempotencyKey) {
        return idempotencyRepo.findById(idempotencyKey)
                .map(entity -> PaymentId.of(entity.getPaymentId()));
    }

    @Override
    public void saveIdempotencyKey(String idempotencyKey, PaymentId paymentId) {
        // native INSERT — throws DataIntegrityViolationException on duplicate key
        idempotencyRepo.insertIdempotencyKey(idempotencyKey, paymentId.value());
    }
}


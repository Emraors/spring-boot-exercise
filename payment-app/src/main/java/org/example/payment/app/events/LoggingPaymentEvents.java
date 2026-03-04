package org.example.payment.app.events;

import org.example.payment.application.port.out.PaymentEvents;
import org.example.payment.domain.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple log-based implementation of PaymentEvents.
 * In production this would publish to a message broker (Kafka, RabbitMQ, etc.).
 */
@Component
public class LoggingPaymentEvents implements PaymentEvents {

    private static final Logger log = LoggerFactory.getLogger(LoggingPaymentEvents.class);

    @Override
    public void publishAuthorized(Payment payment) {
        log.info("EVENT PaymentAuthorized: paymentId={} amount={} {}",
                payment.getId(), payment.getAmount().cents(), payment.getAmount().currency());
    }

    @Override
    public void publishDeclined(Payment payment) {
        log.info("EVENT PaymentDeclined: paymentId={} amount={} {}",
                payment.getId(), payment.getAmount().cents(), payment.getAmount().currency());
    }
}


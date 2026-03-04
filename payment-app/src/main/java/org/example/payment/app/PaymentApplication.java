package org.example.payment.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "org.example.payment.adapter",
        "org.example.payment.app"
})
@EnableJpaRepositories(basePackages = "org.example.payment.adapter.persistence.repository")
@EntityScan(basePackages = "org.example.payment.adapter.persistence.entity")
@EnableCaching
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}

package org.example.payment.adapter.provider;

import org.example.payment.adapter.provider.dto.ProviderAuthRequest;
import org.example.payment.adapter.provider.dto.ProviderAuthResponse;
import org.example.payment.application.port.out.PaymentProvider;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentProviderAdapter implements PaymentProvider {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentProviderAdapter(RestTemplate providerRestTemplate,
                                  @Qualifier("providerBaseUrl") String providerBaseUrl) {
        this.restTemplate = providerRestTemplate;
        this.baseUrl = providerBaseUrl;
    }

    @Override
    public PaymentStatus authorize(Payment payment) {
        ProviderAuthRequest request = new ProviderAuthRequest(
                payment.getAmount().cents(),
                payment.getAmount().currency(),
                payment.getId().toString()
        );

        ProviderAuthResponse response = restTemplate.postForObject(
                baseUrl + "/authorize",
                request,
                ProviderAuthResponse.class
        );

        if (response != null && response.isApproved()) {
            return PaymentStatus.AUTHORIZED;
        }
        return PaymentStatus.DECLINED;
    }
}


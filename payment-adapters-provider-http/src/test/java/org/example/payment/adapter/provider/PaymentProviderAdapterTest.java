package org.example.payment.adapter.provider;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentProviderAdapter with WireMock")
class PaymentProviderAdapterTest {

    private WireMockServer wireMock;
    private PaymentProviderAdapter adapter;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        adapter = new PaymentProviderAdapter(new RestTemplate(), "http://localhost:" + wireMock.port());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    @DisplayName("returns AUTHORIZED when provider responds APPROVED")
    void authorize_approved_returnsAuthorized() {
        wireMock.stubFor(post(urlEqualTo("/authorize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"APPROVED\"}")
                        .withStatus(200)));

        Payment payment = Payment.create(new Money(1000L, "USD"));
        PaymentStatus result = adapter.authorize(payment);

        assertThat(result).isEqualTo(PaymentStatus.AUTHORIZED);
        wireMock.verify(postRequestedFor(urlEqualTo("/authorize")));
    }

    @Test
    @DisplayName("returns DECLINED when provider responds DECLINED")
    void authorize_declined_returnsDeclined() {
        wireMock.stubFor(post(urlEqualTo("/authorize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"DECLINED\"}")
                        .withStatus(200)));

        Payment payment = Payment.create(new Money(99999L, "USD"));
        PaymentStatus result = adapter.authorize(payment);

        assertThat(result).isEqualTo(PaymentStatus.DECLINED);
    }
}


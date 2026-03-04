package org.example.payment.app;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.example.payment.adapter.web.dto.PaymentResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.payment.app.PaymentApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                classes = PaymentApplication.class)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Payment Integration Tests (@SpringBootTest + Testcontainers + WireMock)")
class PaymentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payments")
            .withUsername("payments")
            .withPassword("payments");

    static WireMockServer wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("payment.provider.base-url", () -> "http://localhost:" + wireMock.port());
    }

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetAll();
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/payments";
    }

    private void stubProviderApproved() {
        wireMock.stubFor(post(urlEqualTo("/authorize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"APPROVED\"}")
                        .withStatus(200)));
    }

    private void stubProviderDeclined() {
        wireMock.stubFor(post(urlEqualTo("/authorize"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"DECLINED\"}")
                        .withStatus(200)));
    }

    @Test
    @DisplayName("POST /payments creates payment and GET /payments/{id} returns it")
    void postPayment_thenGetById_returnsCorrectPayment() {
        stubProviderApproved();

        Map<String, Object> request = Map.of(
                "cents", 5000,
                "currency", "USD",
                "idempotencyKey", "it-key-1"
        );

        ResponseEntity<PaymentResponse> createResp = restTemplate.postForEntity(
                baseUrl(), request, PaymentResponse.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PaymentResponse created = createResp.getBody();
        assertThat(created).isNotNull();
        assertThat(created.status()).isEqualTo("AUTHORIZED");
        assertThat(created.cents()).isEqualTo(5000L);
        assertThat(created.currency()).isEqualTo("USD");

        // Now GET the payment
        ResponseEntity<PaymentResponse> getResp = restTemplate.getForEntity(
                baseUrl() + "/" + created.id(), PaymentResponse.class);

        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        PaymentResponse fetched = getResp.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.status()).isEqualTo("AUTHORIZED");
    }

    @Test
    @DisplayName("POST /payments with provider DECLINED results in DECLINED status")
    void postPayment_providerDeclines_statusIsDeclined() {
        stubProviderDeclined();

        Map<String, Object> request = Map.of(
                "cents", 99999,
                "currency", "EUR",
                "idempotencyKey", "it-key-declined"
        );

        ResponseEntity<PaymentResponse> resp = restTemplate.postForEntity(
                baseUrl(), request, PaymentResponse.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().status()).isEqualTo("DECLINED");
    }

    @Test
    @DisplayName("Idempotency: two POSTs with same key return same payment id")
    void postPayment_sameIdempotencyKey_returnsSamePayment() {
        stubProviderApproved();

        Map<String, Object> request = Map.of(
                "cents", 1000,
                "currency", "USD",
                "idempotencyKey", "it-idempotent-key"
        );

        ResponseEntity<PaymentResponse> first = restTemplate.postForEntity(
                baseUrl(), request, PaymentResponse.class);
        ResponseEntity<PaymentResponse> second = restTemplate.postForEntity(
                baseUrl(), request, PaymentResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();
        // Same payment ID returned for both requests
        assertThat(first.getBody().id()).isEqualTo(second.getBody().id());
        // Provider should only be called once
        wireMock.verify(1, postRequestedFor(urlEqualTo("/authorize")));
    }

    @Test
    @DisplayName("GET /payments/{id} returns 404 for unknown payment")
    void getPayment_unknownId_returns404() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                baseUrl() + "/00000000-0000-0000-0000-000000000000", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /payments with invalid body returns 400")
    void postPayment_invalidBody_returns400() {
        Map<String, Object> badRequest = Map.of(
                "cents", -1,
                "currency", "USD",
                "idempotencyKey", "it-bad-key"
        );

        ResponseEntity<String> resp = restTemplate.postForEntity(
                baseUrl(), badRequest, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}


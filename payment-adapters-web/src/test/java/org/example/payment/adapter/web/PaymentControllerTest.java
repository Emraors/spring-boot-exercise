package org.example.payment.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.payment.application.exception.PaymentNotFoundException;
import org.example.payment.application.port.in.CreatePaymentUseCase;
import org.example.payment.application.port.in.CreatePaymentUseCase.CreatePaymentCommand;
import org.example.payment.application.port.in.GetPaymentUseCase;
import org.example.payment.domain.model.Money;
import org.example.payment.domain.model.Payment;
import org.example.payment.domain.model.PaymentId;
import org.example.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {PaymentController.class, GlobalExceptionHandler.class})
@DisplayName("PaymentController @WebMvcTest")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreatePaymentUseCase createPaymentUseCase;

    @MockBean
    private GetPaymentUseCase getPaymentUseCase;

    @Test
    @DisplayName("POST /api/v1/payments returns 201 with payment body")
    void postPayment_validRequest_returns201() throws Exception {
        PaymentId id = PaymentId.newId();
        Payment payment = Payment.reconstitute(id, new Money(1000L, "USD"), PaymentStatus.AUTHORIZED, Instant.now());
        when(createPaymentUseCase.create(any(CreatePaymentCommand.class))).thenReturn(payment);

        String body = objectMapper.writeValueAsString(
                Map.of("cents", 1000, "currency", "USD", "idempotencyKey", "key-1"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.value().toString()))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.cents").value(1000))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("POST /api/v1/payments with negative cents returns 400")
    void postPayment_negativeCents_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("cents", -1, "currency", "USD", "idempotencyKey", "key-2"));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("cents")));
    }

    @Test
    @DisplayName("POST /api/v1/payments with blank idempotencyKey returns 400")
    void postPayment_blankIdempotencyKey_returns400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("cents", 1000, "currency", "USD", "idempotencyKey", ""));

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} returns 200 with payment body")
    void getPayment_found_returns200() throws Exception {
        PaymentId id = PaymentId.newId();
        Payment payment = Payment.reconstitute(id, new Money(500L, "EUR"), PaymentStatus.CREATED, Instant.now());
        when(getPaymentUseCase.getById(any(PaymentId.class))).thenReturn(payment);

        mockMvc.perform(get("/api/v1/payments/{id}", id.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.value().toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("GET /api/v1/payments/{id} returns 404 when not found")
    void getPayment_notFound_returns404() throws Exception {
        PaymentId id = PaymentId.newId();
        when(getPaymentUseCase.getById(any(PaymentId.class)))
                .thenThrow(new PaymentNotFoundException(id));

        mockMvc.perform(get("/api/v1/payments/{id}", id.value()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Payment Not Found"));
    }
}


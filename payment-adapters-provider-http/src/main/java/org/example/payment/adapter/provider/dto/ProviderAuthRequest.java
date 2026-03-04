package org.example.payment.adapter.provider.dto;

public record ProviderAuthRequest(long cents, String currency, String paymentId) {}


package org.example.payment.adapter.provider.dto;

public record ProviderAuthResponse(String status) {
    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }
}


package com.pm.customerservice.dto.payment;

public class CheckoutResponseDTO {
    private String checkoutUrl;
    private String sessionId;

    public CheckoutResponseDTO() {
    }

    public CheckoutResponseDTO(String checkoutUrl, String sessionId) {
        this.checkoutUrl = checkoutUrl;
        this.sessionId = sessionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}

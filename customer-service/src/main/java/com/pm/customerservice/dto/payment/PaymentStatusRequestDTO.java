package com.pm.customerservice.dto.payment;

public class PaymentStatusRequestDTO {
    private String checkoutSessionId;
    private String serviceType;
    private long amount;

    public PaymentStatusRequestDTO() {
    }

    public PaymentStatusRequestDTO(String checkoutSessionId, String serviceType, long amount) {
        this.checkoutSessionId = checkoutSessionId;
        this.serviceType = serviceType;
        this.amount = amount;
    }

    public String getCheckoutSessionId() {
        return checkoutSessionId;
    }

    public void setCheckoutSessionId(String checkoutSessionId) {
        this.checkoutSessionId = checkoutSessionId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}

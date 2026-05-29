package com.pm.customerservice.dto.payment;

public class PaymentStatusResponseDTO {
    private boolean paid;
    private String status;
    private String checkoutSessionId;
    private long amount;
    private String serviceType;

    public PaymentStatusResponseDTO() {
    }

    public PaymentStatusResponseDTO(boolean paid, String status, String checkoutSessionId, long amount, String serviceType) {
        this.paid = paid;
        this.status = status;
        this.checkoutSessionId = checkoutSessionId;
        this.amount = amount;
        this.serviceType = serviceType;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckoutSessionId() {
        return checkoutSessionId;
    }

    public void setCheckoutSessionId(String checkoutSessionId) {
        this.checkoutSessionId = checkoutSessionId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}

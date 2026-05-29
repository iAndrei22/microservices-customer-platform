package com.pm.customerservice.dto.payment;

public class CheckoutRequestDTO {
    private String serviceType;
    private long amount;

    public CheckoutRequestDTO() {
    }

    public CheckoutRequestDTO(String serviceType, long amount) {
        this.serviceType = serviceType;
        this.amount = amount;
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

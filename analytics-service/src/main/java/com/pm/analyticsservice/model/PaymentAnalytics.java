package com.pm.analyticsservice.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "payment_analytics")
public class PaymentAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "session_id", unique = true)
    private String sessionId;

    @Column(name = "event_timestamp")
    private Long eventTimestamp;

    @Column(name = "received_at")
    private Instant receivedAt = Instant.now();

    // Constructors
    public PaymentAnalytics() {}

    public PaymentAnalytics(String customerId, Long amount, String serviceType, 
                            String sessionId, Long eventTimestamp) {
        this.customerId = customerId;
        this.amount = amount;
        this.serviceType = serviceType;
        this.sessionId = sessionId;
        this.eventTimestamp = eventTimestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}

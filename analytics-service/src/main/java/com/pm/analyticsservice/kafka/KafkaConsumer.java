package com.pm.analyticsservice.kafka;

import billing.events.PaymentEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.analyticsservice.model.PaymentAnalytics;
import com.pm.analyticsservice.repository.PaymentAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import customer.events.CustomerEvent;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final PaymentAnalyticsRepository paymentAnalyticsRepository;

    public KafkaConsumer(PaymentAnalyticsRepository paymentAnalyticsRepository) {
        this.paymentAnalyticsRepository = paymentAnalyticsRepository;
    }

    @KafkaListener(topics="customer", groupId = "analytics-service")
    public void consumeCustomerEvent(byte[] event) {
        try {
            CustomerEvent customerEvent = CustomerEvent.parseFrom(event);
            // ... perform any business logic related to analytics here

            log.info("Received Customer Event: [CustomerId={}, CustomerName={}, CustomerEmail={}]",
                    customerEvent.getCustomerId(),
                    customerEvent.getName(),
                    customerEvent.getEmail());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing customer event {}", e.getMessage());
        }
    }

    @KafkaListener(topics="payment-events", groupId = "analytics-service-payments")
    public void consumePaymentEvent(byte[] event) {
        try {
            PaymentEvent paymentEvent = PaymentEvent.parseFrom(event);

            // Check idempotency: skip if sessionId already exists
            if (paymentAnalyticsRepository.existsBySessionId(paymentEvent.getSessionId())) {
                log.warn("Payment event already processed for sessionId={}, skipping", 
                        paymentEvent.getSessionId());
                return;
            }

            // Persist payment event to database
            PaymentAnalytics paymentRecord = new PaymentAnalytics(
                    paymentEvent.getCustomerId(),
                    paymentEvent.getAmount(),
                    paymentEvent.getServiceType(),
                    paymentEvent.getSessionId(),
                    paymentEvent.getTimestamp()
            );

            paymentAnalyticsRepository.save(paymentRecord);

            // Log aggregated metrics
            Long totalAmount = paymentAnalyticsRepository.getTotalPaymentAmount();
            Long totalCount = paymentAnalyticsRepository.getTotalPaymentCount();

            log.info("Received and persisted Payment Event: [CustomerId={}, Amount={}, ServiceType={}, SessionId={}]",
                    paymentEvent.getCustomerId(),
                    paymentEvent.getAmount(),
                    paymentEvent.getServiceType(),
                    paymentEvent.getSessionId());
            log.info("Payment Aggregation - Total Amount: {} cents, Total Payments: {}", 
                    totalAmount != null ? totalAmount : 0, 
                    totalCount != null ? totalCount : 0);

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing payment event {}", e.getMessage());
        }
    }
}

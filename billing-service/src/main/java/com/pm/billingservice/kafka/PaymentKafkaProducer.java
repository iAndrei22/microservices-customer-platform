package com.pm.billingservice.kafka;

import billing.events.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaProducer.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public PaymentKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentEvent(String customerId, long amount, String serviceType, 
                                     String sessionId, long timestamp) {
        PaymentEvent event = PaymentEvent.newBuilder()
                .setCustomerId(customerId)
                .setAmount(amount)
                .setServiceType(serviceType)
                .setSessionId(sessionId)
                .setTimestamp(timestamp)
                .build();

        try {
            kafkaTemplate.send(TOPIC, event.toByteArray());
            log.info("Published payment event to topic '{}': customerId={}, amount={}, serviceType={}, sessionId={}",
                    TOPIC, customerId, amount, serviceType, sessionId);
        } catch (Exception e) {
            log.error("Error publishing payment event for customerId={}, sessionId={}: {}",
                    customerId, sessionId, e.getMessage(), e);
        }
    }
}

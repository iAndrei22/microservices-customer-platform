package com.pm.customerservice.kafka;

import com.pm.customerservice.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import customer.events.CustomerEvent;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(
            KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Customer customer) {
        CustomerEvent event = CustomerEvent.newBuilder()
                .setCustomerId(customer.getId().toString())
                .setName(customer.getName())
                .setEmail(customer.getEmail())
                .setEventType("CUSTOMER_CREATED")
                .build();

        try {
            kafkaTemplate.send("customer", event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending CustomerCreated event: {}", event);
        }
    }

}

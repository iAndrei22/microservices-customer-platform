package com.pm.customerservice.controller;

import com.pm.customerservice.dto.payment.CheckoutRequestDTO;
import com.pm.customerservice.dto.payment.CheckoutResponseDTO;
import com.pm.customerservice.dto.payment.PaymentStatusRequestDTO;
import com.pm.customerservice.dto.payment.PaymentStatusResponseDTO;
import com.pm.customerservice.grpc.BillingServiceGrpcClient;
import com.pm.customerservice.model.Customer;
import com.pm.customerservice.repository.CustomerRepository;
import billing.CheckoutResponse;
import billing.CheckPaymentStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerPaymentController {

    private static final Logger log = LoggerFactory.getLogger(CustomerPaymentController.class);

    private final CustomerRepository customerRepository;
    private final BillingServiceGrpcClient billingClient;

    public CustomerPaymentController(CustomerRepository customerRepository,
                                     BillingServiceGrpcClient billingClient) {
        this.customerRepository = customerRepository;
        this.billingClient = billingClient;
    }

    @PostMapping("/{customerId}/checkout")
    public ResponseEntity<CheckoutResponseDTO> createCheckout(@PathVariable UUID customerId,
                                                              @RequestBody CheckoutRequestDTO request) {

        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CheckoutResponse response = billingClient.createCheckoutSession(customerId.toString(),
                request.getServiceType(), request.getAmount());

        CheckoutResponseDTO dto = new CheckoutResponseDTO(response.getCheckoutUrl(), response.getSessionId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{customerId}/payment/success")
    public ResponseEntity<PaymentStatusResponseDTO> checkPaymentStatus(@PathVariable UUID customerId,
                                                                       @RequestBody PaymentStatusRequestDTO request) {

        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CheckPaymentStatusResponse response = billingClient.checkPaymentStatus(customerId.toString(),
                request.getCheckoutSessionId(), request.getServiceType(), request.getAmount());

        PaymentStatusResponseDTO dto = new PaymentStatusResponseDTO(response.getPaid(), response.getStatus(),
                response.getCheckoutSessionId(), response.getAmount(), response.getServiceType());

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{customerId}/payment/pos")
    public ResponseEntity<PaymentStatusResponseDTO> simulatePosPayment(@PathVariable UUID customerId,
                                                                       @RequestBody CheckoutRequestDTO request) {

        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CheckPaymentStatusResponse response = billingClient.simulatePosPayment(customerId.toString(),
                request.getServiceType(), request.getAmount());

        PaymentStatusResponseDTO dto = new PaymentStatusResponseDTO(response.getPaid(), response.getStatus(),
                response.getCheckoutSessionId(), response.getAmount(), response.getServiceType());

        return ResponseEntity.ok(dto);
    }

}


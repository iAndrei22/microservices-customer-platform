package com.pm.customerservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.CheckoutRequest;
import billing.CheckoutResponse;
import billing.CheckPaymentStatusRequest;
import billing.CheckPaymentStatusResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(
            BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort) {

        log.info("Connecting to Billing Service GRPC service at {}:{}",
                serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,
                serverPort).usePlaintext().build();

        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String customerId, String name,
                                                String email) {

        BillingRequest request = BillingRequest.newBuilder().setCustomerId(customerId)
                .setName(name).setEmail(email).build();

        BillingResponse response = blockingStub.createBillingAccount(request);
        log.info("Received response from billing service via GRPC: {}", response);
        return response;
    }

    public CheckoutResponse createCheckoutSession(String customerId, String serviceType, long amount) {
        CheckoutRequest request = CheckoutRequest.newBuilder()
                .setCustomerId(customerId)
                .setServiceType(serviceType)
                .setAmount(amount)
                .build();

        CheckoutResponse response = blockingStub.createCheckoutSession(request);
        log.info("Received checkout response from billing service via GRPC: {}", response);
        return response;
    }

    public CheckPaymentStatusResponse checkPaymentStatus(String customerId, String checkoutSessionId, String serviceType, long amount) {
        CheckPaymentStatusRequest request = CheckPaymentStatusRequest.newBuilder()
                .setCustomerId(customerId)
                .setCheckoutSessionId(checkoutSessionId)
                .setServiceType(serviceType)
                .setAmount(amount)
                .build();

        CheckPaymentStatusResponse response = blockingStub.checkPaymentStatus(request);
        log.info("Received payment status response from billing service via GRPC: {}", response);
        return response;
    }

        public CheckPaymentStatusResponse simulatePosPayment(String customerId, String serviceType, long amount) {
                CheckoutRequest request = CheckoutRequest.newBuilder()
                                .setCustomerId(customerId)
                                .setServiceType(serviceType)
                                .setAmount(amount)
                                .build();

                CheckPaymentStatusResponse response = blockingStub.simulatePosPayment(request);
                log.info("Received simulate POS response from billing service via GRPC: {}", response);
                return response;
        }
}
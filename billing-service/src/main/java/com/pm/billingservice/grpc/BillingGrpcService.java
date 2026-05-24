package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.repository.BillingAccountRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@GrpcService
@Service
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    private final BillingAccountRepository billingAccountRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    public BillingGrpcService(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @PostConstruct
    public void init() {
        // Initialize Stripe API key from configuration
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe API key initialized (first 8 chars): {}",
                stripeSecretKey == null ? "<null>" : stripeSecretKey.substring(0, Math.min(8, stripeSecretKey.length())) + "...");
    }

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver) {

        log.info("createBillingAccount request received {}", billingRequest.toString());

        String customerId = billingRequest.getCustomerId();
        String name = billingRequest.getName();
        String email = billingRequest.getEmail();

        // Build Stripe customer params
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(name == null || name.isEmpty() ? null : name)
                .setEmail(email == null || email.isEmpty() ? null : email)
                .build();

        try {
            Customer stripeCustomer = Customer.create(params);
            String stripeCustomerId = stripeCustomer.getId();

            // Persist billing account locally in PostgreSQL via Spring Data JPA
            BillingAccount account = new BillingAccount();
            account.setCustomerId(customerId);
            account.setStripeCustomerId(stripeCustomerId);
            account.setStatus("ACTIVE");
            BillingAccount savedAccount = billingAccountRepository.save(account);

            BillingResponse response = BillingResponse.newBuilder()
                    .setAccountId(String.valueOf(savedAccount.getId()))
                    .setStatus(savedAccount.getStatus())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (StripeException e) {
            log.error("Stripe error while creating customer for request {}: {}", billingRequest, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Stripe error: " + e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error while creating billing account: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal error").withCause(e).asRuntimeException());
        }
    }
}

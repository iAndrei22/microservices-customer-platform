package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.CheckPaymentStatusRequest;
import billing.CheckPaymentStatusResponse;
import com.pm.billingservice.kafka.PaymentKafkaProducer;
import com.pm.billingservice.model.BillingAccount;
import com.pm.billingservice.repository.BillingAccountRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
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
    private final PaymentKafkaProducer paymentKafkaProducer;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    public BillingGrpcService(BillingAccountRepository billingAccountRepository,
                              PaymentKafkaProducer paymentKafkaProducer) {
        this.billingAccountRepository = billingAccountRepository;
        this.paymentKafkaProducer = paymentKafkaProducer;
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

    @Override
    public void createCheckoutSession(billing.CheckoutRequest request,
                                      StreamObserver<billing.CheckoutResponse> responseObserver) {

        log.info("createCheckoutSession request received for customerId={}, serviceType={}, amount={}",
                request.getCustomerId(), request.getServiceType(), request.getAmount());

        String customerId = request.getCustomerId();
        String serviceType = request.getServiceType();
        long amount = request.getAmount();

        try {
            // Build price data / product data for the line item
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(serviceType)
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("ron")
                            .setUnitAmount(amount)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(priceData)
                    .setQuantity(1L)
                    .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:3000/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("http://localhost:3000/cancel")
                    .addLineItem(lineItem)
                    .build();

            Session session = Session.create(params);

            String checkoutUrl = session.getUrl() != null ? session.getUrl() : "";
            String sessionId = session.getId() != null ? session.getId() : "";

            billing.CheckoutResponse response = billing.CheckoutResponse.newBuilder()
                    .setCheckoutUrl(checkoutUrl)
                    .setSessionId(sessionId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error creating Stripe Checkout Session for customerId={}, serviceType={}, amount={}: {}",
                    customerId, serviceType, amount, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Stripe error: " + e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @Override
    public void checkPaymentStatus(CheckPaymentStatusRequest request,
                                   StreamObserver<CheckPaymentStatusResponse> responseObserver) {

        log.info("checkPaymentStatus request received for sessionId={}, customerId={}", 
                request.getCheckoutSessionId(), request.getCustomerId());

        String customerId = request.getCustomerId();
        String checkoutSessionId = request.getCheckoutSessionId();
        String serviceType = request.getServiceType();
        long amount = request.getAmount();

        try {
            // Retrieve session from Stripe to check payment status
            Session session = Session.retrieve(checkoutSessionId);
            
            boolean isPaid = "paid".equals(session.getPaymentStatus());
            String status = session.getPaymentStatus();

            log.info("Stripe session {} status: {}", checkoutSessionId, status);

            // If payment is confirmed, publish Kafka event
            if (isPaid) {
                long currentTimestamp = System.currentTimeMillis();
                paymentKafkaProducer.publishPaymentEvent(
                        customerId,
                        amount,
                        serviceType,
                        checkoutSessionId,
                        currentTimestamp
                );
                log.info("Payment confirmed and event published for customerId={}, sessionId={}", 
                        customerId, checkoutSessionId);
            }

            CheckPaymentStatusResponse response = CheckPaymentStatusResponse.newBuilder()
                    .setPaid(isPaid)
                    .setStatus(status)
                    .setCheckoutSessionId(checkoutSessionId)
                    .setAmount(amount)
                    .setServiceType(serviceType)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (StripeException e) {
            log.error("Stripe error while checking payment status for sessionId={}: {}", 
                    checkoutSessionId, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Stripe error: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Unexpected error while checking payment status: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}

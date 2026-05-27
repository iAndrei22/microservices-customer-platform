package com.pm.analyticsservice.repository;

import com.pm.analyticsservice.model.PaymentAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAnalyticsRepository extends JpaRepository<PaymentAnalytics, Long> {
    boolean existsBySessionId(String sessionId);

    @Query("SELECT SUM(p.amount) FROM PaymentAnalytics p")
    Long getTotalPaymentAmount();

    @Query("SELECT COUNT(p) FROM PaymentAnalytics p")
    Long getTotalPaymentCount();
}

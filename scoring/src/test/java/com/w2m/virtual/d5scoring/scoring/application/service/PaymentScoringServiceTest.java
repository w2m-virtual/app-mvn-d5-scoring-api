package com.w2m.virtual.d5scoring.scoring.application.service;

import com.w2m.virtual.d5scoring.scoring.domain.PaymentAggregate;
import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.output.inmemory.InMemoryPaymentAggregateRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentScoringServiceTest {

    private final PaymentScoringService service =
            new PaymentScoringService(new InMemoryPaymentAggregateRepository());

    @Test
    void applyCaptured_incrementsCounterAndSumByCurrency() {
        service.applyCaptured(new BigDecimal("100.00"), "EUR");
        service.applyCaptured(new BigDecimal("50.00"), "EUR");
        service.applyCaptured(new BigDecimal("30.00"), "USD");

        PaymentAggregate agg = service.get();
        assertThat(agg.captured()).isEqualTo(3);
        assertThat(agg.refunded()).isZero();
        assertThat(agg.totalCapturedAmountByCurrency().get("EUR")).isEqualByComparingTo("150.00");
        assertThat(agg.totalCapturedAmountByCurrency().get("USD")).isEqualByComparingTo("30.00");
        assertThat(agg.lastUpdatedAt()).isNotNull();
    }

    @Test
    void applyRefunded_incrementsCounterAndSumByCurrency() {
        service.applyRefunded(new BigDecimal("25.00"), "EUR");
        service.applyRefunded(new BigDecimal("10.00"), "EUR");

        PaymentAggregate agg = service.get();
        assertThat(agg.refunded()).isEqualTo(2);
        assertThat(agg.captured()).isZero();
        assertThat(agg.totalRefundedAmountByCurrency().get("EUR")).isEqualByComparingTo("35.00");
    }

    @Test
    void capturedAndRefunded_areTrackedIndependently() {
        service.applyCaptured(new BigDecimal("100"), "EUR");
        service.applyCaptured(new BigDecimal("100"), "EUR");
        service.applyRefunded(new BigDecimal("100"), "EUR");

        PaymentAggregate agg = service.get();
        assertThat(agg.captured()).isEqualTo(2);
        assertThat(agg.refunded()).isEqualTo(1);
        assertThat(agg.totalCapturedAmountByCurrency().get("EUR")).isEqualByComparingTo("200");
        assertThat(agg.totalRefundedAmountByCurrency().get("EUR")).isEqualByComparingTo("100");
    }

    @Test
    void applyCaptured_withNullAmount_stillIncrementsCounter() {
        service.applyCaptured(null, "EUR");
        PaymentAggregate agg = service.get();
        assertThat(agg.captured()).isEqualTo(1);
        assertThat(agg.totalCapturedAmountByCurrency()).isEmpty();
    }
}

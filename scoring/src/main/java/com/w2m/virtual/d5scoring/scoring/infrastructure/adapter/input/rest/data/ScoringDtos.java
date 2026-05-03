package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest.data;

import com.w2m.virtual.d5scoring.scoring.domain.PaymentAggregate;
import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class ScoringDtos {

    private ScoringDtos() {}

    public record PaymentAggregateResponse(
            long captured,
            long refunded,
            Map<String, BigDecimal> totalCapturedAmountByCurrency,
            Map<String, BigDecimal> totalRefundedAmountByCurrency,
            Instant lastUpdatedAt
    ) {
        public static PaymentAggregateResponse from(PaymentAggregate a) {
            return new PaymentAggregateResponse(
                    a.captured(),
                    a.refunded(),
                    a.totalCapturedAmountByCurrency(),
                    a.totalRefundedAmountByCurrency(),
                    a.lastUpdatedAt()
            );
        }
    }

    public record SupplierScoreResponse(
            UUID supplierId,
            long totalAttempts,
            long confirmed,
            long rejected,
            int score,
            Instant lastUpdatedAt
    ) {
        public static SupplierScoreResponse from(SupplierScore s) {
            return new SupplierScoreResponse(
                    s.supplierId(), s.totalAttempts(), s.confirmed(), s.rejected(), s.score(), s.lastUpdatedAt()
            );
        }
    }
}

package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest.data;

import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;

import java.time.Instant;
import java.util.UUID;

public final class ScoringDtos {

    private ScoringDtos() {}

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

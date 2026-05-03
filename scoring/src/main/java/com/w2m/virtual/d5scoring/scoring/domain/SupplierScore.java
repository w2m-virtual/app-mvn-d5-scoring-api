package com.w2m.virtual.d5scoring.scoring.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Score agregado de un supplier.
 *
 * <p>Fórmula del score:
 * {@code score = round( (confirmed - rejected * 3.0) / max(1, totalAttempts) * 100 )},
 * saturado al rango {@code [0..100]}.</p>
 */
public record SupplierScore(
        UUID supplierId,
        long totalAttempts,
        long confirmed,
        long rejected,
        int score,
        Instant lastUpdatedAt
) {
    public static SupplierScore initial(UUID supplierId, Instant now) {
        return new SupplierScore(supplierId, 0L, 0L, 0L, 0, now);
    }
}

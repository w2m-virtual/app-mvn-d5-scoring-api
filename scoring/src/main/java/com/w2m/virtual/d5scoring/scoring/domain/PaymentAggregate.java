package com.w2m.virtual.d5scoring.scoring.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agregado global de pagos — contadores y totales por moneda.
 *
 * <p>No hay clave por supplier porque el modelo simple de payments no asocia supplierId.</p>
 */
public record PaymentAggregate(
        long captured,
        long refunded,
        Map<String, BigDecimal> totalCapturedAmountByCurrency,
        Map<String, BigDecimal> totalRefundedAmountByCurrency,
        Instant lastUpdatedAt
) {
    public static PaymentAggregate empty() {
        return new PaymentAggregate(0L, 0L, new LinkedHashMap<>(), new LinkedHashMap<>(), null);
    }
}

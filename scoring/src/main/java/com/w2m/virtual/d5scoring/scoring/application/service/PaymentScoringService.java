package com.w2m.virtual.d5scoring.scoring.application.service;

import com.w2m.virtual.d5scoring.scoring.application.port.input.GetPaymentAggregateInputPort;
import com.w2m.virtual.d5scoring.scoring.application.port.output.PaymentAggregateRepositoryPort;
import com.w2m.virtual.d5scoring.scoring.domain.PaymentAggregate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application service del agregado global de pagos.
 *
 * <p>Suma capturas y reembolsos por moneda; mantiene contadores totales.</p>
 */
@Service
public class PaymentScoringService implements GetPaymentAggregateInputPort {

    private final PaymentAggregateRepositoryPort repository;

    public PaymentScoringService(PaymentAggregateRepositoryPort repository) {
        this.repository = repository;
    }

    public synchronized PaymentAggregate applyCaptured(BigDecimal amount, String currency) {
        PaymentAggregate current = repository.get();
        Map<String, BigDecimal> capturedMap = new LinkedHashMap<>(current.totalCapturedAmountByCurrency());
        addAmount(capturedMap, amount, currency);
        PaymentAggregate updated = new PaymentAggregate(
                current.captured() + 1,
                current.refunded(),
                capturedMap,
                new LinkedHashMap<>(current.totalRefundedAmountByCurrency()),
                Instant.now()
        );
        return repository.update(updated);
    }

    public synchronized PaymentAggregate applyRefunded(BigDecimal amount, String currency) {
        PaymentAggregate current = repository.get();
        Map<String, BigDecimal> refundedMap = new LinkedHashMap<>(current.totalRefundedAmountByCurrency());
        addAmount(refundedMap, amount, currency);
        PaymentAggregate updated = new PaymentAggregate(
                current.captured(),
                current.refunded() + 1,
                new LinkedHashMap<>(current.totalCapturedAmountByCurrency()),
                refundedMap,
                Instant.now()
        );
        return repository.update(updated);
    }

    private static void addAmount(Map<String, BigDecimal> map, BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return;
        }
        map.merge(currency, amount, BigDecimal::add);
    }

    @Override
    public PaymentAggregate get() {
        return repository.get();
    }
}

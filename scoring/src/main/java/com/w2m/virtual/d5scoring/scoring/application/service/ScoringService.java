package com.w2m.virtual.d5scoring.scoring.application.service;

import com.w2m.virtual.d5scoring.scoring.application.port.input.GetSupplierScoreInputPort;
import com.w2m.virtual.d5scoring.scoring.application.port.input.ListSupplierScoresInputPort;
import com.w2m.virtual.d5scoring.scoring.application.port.output.SupplierScoreRepositoryPort;
import com.w2m.virtual.d5scoring.scoring.domain.BookingEvent;
import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service del subdominio scoring — aplica eventos al agregado por supplier.
 *
 * <p>Fórmula:
 * {@code score = round( (confirmed - rejected * 3.0) / max(1, totalAttempts) * 100 )},
 * saturado a {@code [0..100]}.</p>
 */
@Service
public class ScoringService implements GetSupplierScoreInputPort, ListSupplierScoresInputPort {

    private final SupplierScoreRepositoryPort repository;

    public ScoringService(SupplierScoreRepositoryPort repository) {
        this.repository = repository;
    }

    public synchronized SupplierScore applyEvent(BookingEvent event) {
        if (event == null || event.supplierId() == null || event.status() == null) {
            throw new IllegalArgumentException("event, supplierId and status are required");
        }
        UUID supplierId = event.supplierId();
        SupplierScore current = repository.findById(supplierId)
                .orElse(SupplierScore.initial(supplierId, event.occurredAt() != null ? event.occurredAt() : Instant.now()));

        long totalAttempts = current.totalAttempts() + 1;
        long confirmed = current.confirmed();
        long rejected = current.rejected();

        if (BookingEvent.CONFIRMED.equalsIgnoreCase(event.status())) {
            confirmed += 1;
        } else if (BookingEvent.REJECTED.equalsIgnoreCase(event.status())) {
            rejected += 1;
        } else {
            // status desconocido: contamos el intento pero no movemos contadores
        }

        int score = computeScore(totalAttempts, confirmed, rejected);
        Instant now = event.occurredAt() != null ? event.occurredAt() : Instant.now();
        SupplierScore updated = new SupplierScore(supplierId, totalAttempts, confirmed, rejected, score, now);
        return repository.save(updated);
    }

    static int computeScore(long totalAttempts, long confirmed, long rejected) {
        long denom = Math.max(1L, totalAttempts);
        double raw = ((double) confirmed - rejected * 3.0) / denom * 100.0;
        long rounded = Math.round(raw);
        if (rounded < 0) rounded = 0;
        if (rounded > 100) rounded = 100;
        return (int) rounded;
    }

    @Override
    public Optional<SupplierScore> getBySupplierId(UUID supplierId) {
        return repository.findById(supplierId);
    }

    @Override
    public List<SupplierScore> listAll() {
        return repository.findAll();
    }
}

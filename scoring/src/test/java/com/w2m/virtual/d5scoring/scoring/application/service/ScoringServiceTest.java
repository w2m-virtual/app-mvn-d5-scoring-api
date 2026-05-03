package com.w2m.virtual.d5scoring.scoring.application.service;

import com.w2m.virtual.d5scoring.scoring.domain.BookingEvent;
import com.w2m.virtual.d5scoring.scoring.domain.SupplierScore;
import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.output.inmemory.InMemoryScoreRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoringServiceTest {

    @Test
    void threeConfirmed_yieldsScore100() {
        ScoringService svc = new ScoringService(new InMemoryScoreRepository());
        UUID supplierId = UUID.randomUUID();
        for (int i = 0; i < 3; i++) {
            svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h-1", BookingEvent.CONFIRMED, Instant.now()));
        }
        Optional<SupplierScore> s = svc.getBySupplierId(supplierId);
        assertTrue(s.isPresent());
        assertEquals(3L, s.get().totalAttempts());
        assertEquals(3L, s.get().confirmed());
        assertEquals(0L, s.get().rejected());
        assertEquals(100, s.get().score());
    }

    @Test
    void confirmedThenRejected_lowersScore() {
        ScoringService svc = new ScoringService(new InMemoryScoreRepository());
        UUID supplierId = UUID.randomUUID();
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h-1", BookingEvent.CONFIRMED, Instant.now()));
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h-1", BookingEvent.CONFIRMED, Instant.now()));
        SupplierScore afterConfirmed = svc.getBySupplierId(supplierId).orElseThrow();
        assertEquals(100, afterConfirmed.score());

        svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h-1", BookingEvent.REJECTED, Instant.now()));
        SupplierScore afterRejected = svc.getBySupplierId(supplierId).orElseThrow();
        assertEquals(3L, afterRejected.totalAttempts());
        assertEquals(2L, afterRejected.confirmed());
        assertEquals(1L, afterRejected.rejected());
        // (2 - 3) / 3 * 100 = -33.33 → saturado a 0
        assertEquals(0, afterRejected.score());
    }

    @Test
    void emptyRepo_returnsEmptyList() {
        ScoringService svc = new ScoringService(new InMemoryScoreRepository());
        List<SupplierScore> all = svc.listAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void mixedEvents_computesDeterministicScore() {
        ScoringService svc = new ScoringService(new InMemoryScoreRepository());
        UUID supplierId = UUID.randomUUID();
        // 4 confirmados, 1 rechazado: (4 - 3) / 5 * 100 = 20
        for (int i = 0; i < 4; i++) {
            svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h", BookingEvent.CONFIRMED, Instant.now()));
        }
        svc.applyEvent(new BookingEvent(UUID.randomUUID(), supplierId, "h", BookingEvent.REJECTED, Instant.now()));
        SupplierScore s = svc.getBySupplierId(supplierId).orElseThrow();
        assertEquals(5L, s.totalAttempts());
        assertEquals(20, s.score());
    }
}

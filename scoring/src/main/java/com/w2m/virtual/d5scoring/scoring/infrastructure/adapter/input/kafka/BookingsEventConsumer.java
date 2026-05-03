package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.kafka;

import com.w2m.virtual.d5scoring.scoring.application.service.ScoringService;
import com.w2m.virtual.d5scoring.scoring.domain.BookingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adapter input — consume eventos de bookings.events y actualiza el scoring.
 */
@Component
public class BookingsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingsEventConsumer.class);

    private final ScoringService scoringService;

    public BookingsEventConsumer(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @KafkaListener(topics = "${scoring.topic}", groupId = "d5-scoring")
    public void onBookingEvent(BookingEvent event) {
        try {
            log.info("BookingEvent received supplierId={} status={}", event.supplierId(), event.status());
            scoringService.applyEvent(event);
        } catch (Exception ex) {
            log.warn("Failed to apply BookingEvent: {}", ex.getMessage(), ex);
        }
    }
}

package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter input — consume eventos {@code hotels.events} (publicados por D2) y los
 * registra en {@link HotelsTracker}. Bloque B+.
 */
@Component
public class HotelsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(HotelsEventConsumer.class);

    private final HotelsTracker tracker;

    public HotelsEventConsumer(HotelsTracker tracker) {
        this.tracker = tracker;
    }

    @KafkaListener(topics = "${hotels.topic:hotels.events}",
            groupId = "d5-scoring-hotels",
            containerFactory = "hotelsKafkaListenerContainerFactory")
    public void onHotelEvent(Map<String, Object> event) {
        try {
            log.info("D5 observa HotelEvent type={} hotelId={} active={}",
                    event.get("type"), event.get("hotelId"), event.get("active"));
            tracker.record(event);
        } catch (Exception ex) {
            log.warn("Failed to record HotelEvent: {}", ex.getMessage(), ex);
        }
    }
}

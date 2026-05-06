package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.rest;

import com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.kafka.HotelsTracker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint de visibilidad: últimos eventos {@code hotels.events} observados por D5.
 * Útil para tests E2E (verificar la propagación D2 → broker → D5 sin abrir el broker).
 */
@RestController
@RequestMapping("/api/events/hotels")
public class HotelsEventsRestAdapter {

    private final HotelsTracker tracker;

    public HotelsEventsRestAdapter(HotelsTracker tracker) {
        this.tracker = tracker;
    }

    @GetMapping
    public List<Map<String, Object>> recent() {
        return tracker.recent();
    }

    @GetMapping("/count")
    public Map<String, Object> count() {
        return Map.of("count", tracker.size());
    }
}

package com.w2m.virtual.d5scoring.scoring.infrastructure.adapter.input.kafka;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cola in-memory acotada de los últimos eventos {@code hotels.events} observados desde
 * D2. Bloque B+: D5 mantiene visibilidad sobre el inventario de hoteles para futuras
 * extensiones del scoring (por hotel, no solo por supplier). La fórmula de scoring por
 * hotel queda fuera de scope (decisión de dominio del technical-designer-back).
 */
@Component
public class HotelsTracker {

    private static final int MAX = 100;
    private final Deque<Map<String, Object>> recent = new ArrayDeque<>();

    public synchronized void record(Map<String, Object> event) {
        Map<String, Object> entry = new LinkedHashMap<>(event);
        entry.putIfAbsent("seenAt", Instant.now().toString());
        recent.addFirst(entry);
        while (recent.size() > MAX) {
            recent.removeLast();
        }
    }

    public synchronized List<Map<String, Object>> recent() {
        return List.copyOf(recent);
    }

    public synchronized int size() {
        return recent.size();
    }
}

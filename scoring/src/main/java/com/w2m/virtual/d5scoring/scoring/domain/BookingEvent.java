package com.w2m.virtual.d5scoring.scoring.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado por supplier-api a {@code bookings.events} tras intentar una reserva.
 *
 * <p>{@code status} es uno de {@code "CONFIRMED"} | {@code "REJECTED"}.</p>
 */
public record BookingEvent(
        UUID bookingId,
        UUID supplierId,
        String hotelId,
        String status,
        Instant occurredAt
) {
    public static final String CONFIRMED = "CONFIRMED";
    public static final String REJECTED = "REJECTED";
}

package com.ticketbooking.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Параметры GET /api/concert/concerts. Все поля необязательные.
 * sort: date (по умолчанию), rating, duration, durationDesc.
 */
public record ConcertFilter(
        String q,
        String city,
        String genre,
        Double minRating,
        Integer minDuration,
        Integer maxDuration,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        String sort
) {
    public static ConcertFilter empty() {
        return new ConcertFilter(null, null, null, null, null, null, null, null, null);
    }
}

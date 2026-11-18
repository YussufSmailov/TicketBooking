package com.ticketbooking.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Параметры GET /api/cinema/movies. Все поля необязательные.
 * sort: rating (по умолчанию), title, duration, durationDesc, release.
 */
public record MovieFilter(
        String q,
        String genre,
        String ageRating,
        Double minRating,
        Integer minDuration,
        Integer maxDuration,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        String sort
) {
    public static MovieFilter empty() {
        return new MovieFilter(null, null, null, null, null, null, null, null);
    }
}

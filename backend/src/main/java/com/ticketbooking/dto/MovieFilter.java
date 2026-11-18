package com.ticketbooking.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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

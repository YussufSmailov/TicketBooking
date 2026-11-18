package com.ticketbooking.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MatchFilter(
        String q,
        Long teamId,
        Double minRating,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        String sort
) {
    public static MatchFilter empty() {
        return new MatchFilter(null, null, null, null, null, null);
    }
}

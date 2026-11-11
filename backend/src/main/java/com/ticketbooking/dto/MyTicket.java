package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MyTicket(
        String type,
        Long bookingId,
        Long eventId,
        String eventTitle,
        LocalDate eventDate,
        LocalTime eventTime,
        String place,
        String seatNumber,
        String zone,
        BigDecimal price,
        String status,
        LocalDateTime bookingDate,
        String ticketNumber
) {
}

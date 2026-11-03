package com.ticketbooking.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionRequest(Long movieId, LocalDate sessionDate, LocalTime sessionTime, Integer hallNumber) {
}

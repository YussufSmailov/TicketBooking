package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record HomeCard(String type, Long id, String title, String subtitle,
                       LocalDate date, LocalTime time, String posterUrl, BigDecimal minPrice,
                       Double rating, Integer reviewCount, String homeLogoUrl, String awayLogoUrl) {
}

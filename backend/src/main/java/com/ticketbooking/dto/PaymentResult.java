package com.ticketbooking.dto;

import java.math.BigDecimal;

public record PaymentResult(String type, Long bookingId, BigDecimal amount,
                            String paymentStatus, String bookingStatus, String ticketNumber, String message) {
}

package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentResult(String type, Long bookingId, BigDecimal amount,
                            String paymentStatus, String bookingStatus, String ticketNumber, String message,
                            List<Long> bookingIds, List<String> ticketNumbers) {
}

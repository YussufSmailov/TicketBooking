package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * bookingId и ticketNumber — первой брони заказа (для совместимости), bookingIds и ticketNumbers — всех.
 * amount — общая сумма заказа.
 */
public record PaymentResult(String type, Long bookingId, BigDecimal amount,
                            String paymentStatus, String bookingStatus, String ticketNumber, String message,
                            List<Long> bookingIds, List<String> ticketNumbers) {
}

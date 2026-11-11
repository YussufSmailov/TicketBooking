package com.ticketbooking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingRequest(
        @NotNull(message = "Выберите место")
        @Positive(message = "Неверный номер места")
        Long seatId
) {
}

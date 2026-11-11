package com.ticketbooking.dto;

import jakarta.validation.Valid;

public record PaymentRequest(
        Long cardId,

        @Valid
        CardRequest newCard,

        boolean saveCard
) {
}

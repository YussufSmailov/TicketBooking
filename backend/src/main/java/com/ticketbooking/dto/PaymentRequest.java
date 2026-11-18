package com.ticketbooking.dto;

import jakarta.validation.Valid;

import java.util.List;

public record PaymentRequest(
        Long cardId,

        @Valid
        CardRequest newCard,

        boolean saveCard,

        List<Long> bookingIds
) {
    public PaymentRequest(Long cardId, CardRequest newCard, boolean saveCard) {
        this(cardId, newCard, saveCard, null);
    }
}

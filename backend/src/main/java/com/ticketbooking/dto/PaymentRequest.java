package com.ticketbooking.dto;

import jakarta.validation.Valid;

import java.util.List;

/**
 * bookingIds нужен только для POST /api/payments/{type} (оплата сразу нескольких броней одной картой).
 */
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

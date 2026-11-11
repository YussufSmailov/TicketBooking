package com.ticketbooking.dto;

public record CardResponse(Long cardId, String holderName, String brand, String last4,
                           Integer expMonth, Integer expYear, String title) {
}

package com.ticketbooking.dto;

public record VerificationSentResponse(String email, String message, long resendAfterSeconds, long codeMinutes) {
}

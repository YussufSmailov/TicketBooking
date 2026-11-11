package com.ticketbooking.dto;

public record AuthResponse(String token, UserResponse user) {
}

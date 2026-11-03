package com.ticketbooking.dto;

public record BookingRequest(String fullName, String email, String phone, Long seatId) {
}

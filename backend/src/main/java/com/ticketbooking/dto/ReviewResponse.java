package com.ticketbooking.dto;

import java.time.LocalDateTime;

public record ReviewResponse(Long reviewId, String userName, int rating, String comment,
                             LocalDateTime createdAt, LocalDateTime updatedAt, boolean mine) {
}

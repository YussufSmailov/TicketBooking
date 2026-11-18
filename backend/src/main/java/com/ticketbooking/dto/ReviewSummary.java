package com.ticketbooking.dto;

import java.util.List;

public record ReviewSummary(String eventType, Long eventId, Double average, int count,
                            List<Integer> distribution, List<ReviewResponse> reviews) {
}

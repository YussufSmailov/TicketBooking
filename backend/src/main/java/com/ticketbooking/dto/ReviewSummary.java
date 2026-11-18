package com.ticketbooking.dto;

import java.util.List;

/**
 * distribution — сколько оценок каждого балла: элемент 0 — единицы, элемент 4 — пятёрки.
 */
public record ReviewSummary(String eventType, Long eventId, Double average, int count,
                            List<Integer> distribution, List<ReviewResponse> reviews) {
}

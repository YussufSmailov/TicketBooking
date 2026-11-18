package com.ticketbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "Поставьте оценку")
        @Min(value = 1, message = "Оценка от 1 до 5")
        @Max(value = 5, message = "Оценка от 1 до 5")
        Integer rating,

        @Size(max = 1000, message = "Отзыв не длиннее 1000 символов")
        String comment
) {
}

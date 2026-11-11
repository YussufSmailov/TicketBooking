package com.ticketbooking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionRequest(
        @NotNull(message = "Выберите фильм")
        Long movieId,

        @NotNull(message = "Введите дату сеанса")
        @FutureOrPresent(message = "Дата сеанса не может быть в прошлом")
        LocalDate sessionDate,

        @NotNull(message = "Введите время сеанса")
        LocalTime sessionTime,

        @NotNull(message = "Введите номер зала")
        @Positive(message = "Номер зала должен быть больше 0")
        @Max(value = 20, message = "Номер зала не больше 20")
        Integer hallNumber
) {
}

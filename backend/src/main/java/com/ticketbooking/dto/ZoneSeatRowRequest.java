package com.ticketbooking.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ZoneSeatRowRequest(
        @NotBlank(message = "Введите зону или сектор")
        @Size(max = 30, message = "Не длиннее 30 символов")
        String zone,

        @NotNull(message = "Введите номер ряда")
        @Positive(message = "Номер ряда должен быть больше 0")
        @Max(value = 50, message = "Номер ряда не больше 50")
        Integer rowNumber,

        @NotNull(message = "Введите количество мест")
        @Positive(message = "Количество мест должно быть больше 0")
        @Max(value = 60, message = "Не больше 60 мест в ряду")
        Integer seatCount,

        @NotNull(message = "Введите цену")
        @Positive(message = "Цена должна быть больше 0")
        @DecimalMax(value = "10000000", message = "Слишком большая цена")
        BigDecimal price
) {
}

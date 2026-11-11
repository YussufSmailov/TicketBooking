package com.ticketbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardRequest(
        @NotBlank(message = "Введите номер карты")
        @Pattern(regexp = "^[0-9 ]{13,23}$", message = "Номер карты: от 13 до 19 цифр")
        String cardNumber,

        @NotBlank(message = "Введите имя владельца")
        @Size(min = 2, max = 100, message = "Имя владельца от 2 до 100 символов")
        @Pattern(regexp = "^[A-Za-z \\-]+$", message = "Имя владельца латиницей, как на карте")
        String holderName,

        @NotNull(message = "Введите месяц")
        @Min(value = 1, message = "Месяц от 1 до 12")
        @Max(value = 12, message = "Месяц от 1 до 12")
        Integer expMonth,

        @NotNull(message = "Введите год")
        @Min(value = 2000, message = "Неверный год")
        @Max(value = 2100, message = "Неверный год")
        Integer expYear,

        @NotBlank(message = "Введите CVV")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV: 3 или 4 цифры")
        String cvv
) {

    @Override
    public String toString() {
        return "CardRequest[hidden]";
    }
}

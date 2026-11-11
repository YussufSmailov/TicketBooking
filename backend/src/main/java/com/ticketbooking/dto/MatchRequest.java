package com.ticketbooking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchRequest(
        @NotNull(message = "Выберите команду хозяев")
        Long homeTeamId,

        @NotNull(message = "Выберите команду гостей")
        Long awayTeamId,

        @NotBlank(message = "Введите турнир")
        @Size(max = 100, message = "Не длиннее 100 символов")
        String tournament,

        @Size(max = 30, message = "Не длиннее 30 символов")
        String roundName,

        @NotNull(message = "Введите дату матча")
        @FutureOrPresent(message = "Дата матча не может быть в прошлом")
        LocalDate matchDate,

        @NotNull(message = "Введите время матча")
        LocalTime matchTime,

        @Size(max = 2000, message = "Описание не длиннее 2000 символов")
        String description,

        @Size(max = 300, message = "Ссылка не длиннее 300 символов")
        @Pattern(regexp = "^(https?://\\S+)?$", message = "Ссылка на постер должна начинаться с http:// или https://")
        String posterUrl
) {
}

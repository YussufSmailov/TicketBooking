package com.ticketbooking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ConcertRequest(
        @NotBlank(message = "Введите исполнителя")
        @Size(max = 100, message = "Не длиннее 100 символов")
        String artist,

        @NotBlank(message = "Введите название концерта")
        @Size(max = 150, message = "Не длиннее 150 символов")
        String title,

        @NotBlank(message = "Введите площадку")
        @Size(max = 100, message = "Не длиннее 100 символов")
        String venue,

        @NotBlank(message = "Введите город")
        @Size(max = 50, message = "Не длиннее 50 символов")
        String city,

        @NotNull(message = "Введите дату концерта")
        @FutureOrPresent(message = "Дата концерта не может быть в прошлом")
        LocalDate concertDate,

        @NotNull(message = "Введите время концерта")
        LocalTime concertTime,

        @Positive(message = "Длительность должна быть больше 0")
        @Max(value = 600, message = "Длительность не больше 600 минут")
        Integer durationMin,

        @Size(max = 50, message = "Жанр не длиннее 50 символов")
        String genre,

        @Size(max = 2000, message = "Описание не длиннее 2000 символов")
        String description,

        @Size(max = 300, message = "Ссылка не длиннее 300 символов")
        @Pattern(regexp = ValidationRules.POSTER_URL, message = ValidationRules.POSTER_URL_MESSAGE)
        String posterUrl
) {
}

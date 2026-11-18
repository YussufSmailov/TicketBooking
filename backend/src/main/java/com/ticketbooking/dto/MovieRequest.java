package com.ticketbooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MovieRequest(
        @NotBlank(message = "Введите название фильма")
        @Size(max = 150, message = "Название не длиннее 150 символов")
        String title,

        @NotNull(message = "Введите длительность")
        @Positive(message = "Длительность должна быть больше 0")
        @Max(value = 600, message = "Длительность не больше 600 минут")
        Integer durationMin,

        @NotBlank(message = "Введите возрастной рейтинг")
        @Pattern(regexp = ValidationRules.AGE_RATING, message = "Рейтинг в формате 0+, 6+, 12+, 14+, 16+, 18+ или 21+")
        String ageRating,

        @Size(max = 50, message = "Жанр не длиннее 50 символов")
        String genre,

        LocalDate releaseDate,

        boolean premiere,

        @Size(max = 2000, message = "Описание не длиннее 2000 символов")
        String description,

        @Size(max = 300, message = "Ссылка не длиннее 300 символов")
        @Pattern(regexp = ValidationRules.POSTER_URL, message = ValidationRules.POSTER_URL_MESSAGE)
        String posterUrl
) {
}

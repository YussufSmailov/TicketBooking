package com.ticketbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
        @NotBlank(message = "Введите email")
        @Pattern(regexp = ValidationRules.EMAIL, message = ValidationRules.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Введите код из письма")
        @Pattern(regexp = "^[0-9]{6}$", message = "Код состоит из 6 цифр")
        String code
) {
}

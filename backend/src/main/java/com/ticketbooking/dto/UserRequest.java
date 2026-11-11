package com.ticketbooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Введите имя")
        @Size(min = 2, max = 100, message = ValidationRules.NAME_SIZE_MESSAGE)
        @Pattern(regexp = ValidationRules.NAME, message = ValidationRules.NAME_MESSAGE)
        String fullName,

        @NotBlank(message = "Введите email")
        @Email(message = ValidationRules.EMAIL_MESSAGE)
        @Pattern(regexp = ValidationRules.EMAIL, message = ValidationRules.EMAIL_MESSAGE)
        String email,

        @NotBlank(message = "Введите телефон")
        @Pattern(regexp = ValidationRules.PHONE, message = ValidationRules.PHONE_MESSAGE)
        String phone
) {
}

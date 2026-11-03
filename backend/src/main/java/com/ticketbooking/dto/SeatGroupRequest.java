package com.ticketbooking.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public record SeatGroupRequest(String name, Integer count, BigDecimal price) {

    public void validate() {
        if (name == null || name.isBlank() || name.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Введите название ряда, зоны или сектора (до 20 символов)");
        }
        if (count == null || count < 1 || count > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Количество мест должно быть от 1 до 50");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Цена должна быть больше 0");
        }
    }
}

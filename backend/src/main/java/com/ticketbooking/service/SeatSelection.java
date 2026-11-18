package com.ticketbooking.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SeatSelection {

    private SeatSelection() {
    }

    public static void check(List<Long> seatIds, int maxSeats) {
        if (seatIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Выберите место");
        }
        if (seatIds.size() > maxSeats) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "За один заказ можно выбрать не больше " + maxSeats + " мест");
        }
        Set<Long> unique = new HashSet<>();
        for (Long id : seatIds) {
            if (id == null || id <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный номер места");
            }
            if (!unique.add(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Одно и то же место выбрано дважды");
            }
        }
    }

    public static void checkAllFound(int requested, int found) {
        if (requested != found) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено");
        }
    }

    public static void checkSameEvent(Set<Long> eventIds) {
        if (eventIds.size() > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Все места в заказе должны быть на одно событие");
        }
    }

    public static void checkAvailable(String status) {
        if (!status.equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Одно из выбранных мест уже занято, обновите схему");
        }
    }
}

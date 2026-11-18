package com.ticketbooking.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Можно передать одно место {"seatId": 5} или несколько {"seatIds": [5, 6, 7]}.
 */
public record BookingRequest(
        Long seatId,
        List<Long> seatIds
) {
    public List<Long> allSeatIds() {
        List<Long> ids = new ArrayList<>();
        if (seatIds != null) {
            ids.addAll(seatIds);
        }
        if (seatId != null) {
            ids.add(seatId);
        }
        return ids;
    }
}

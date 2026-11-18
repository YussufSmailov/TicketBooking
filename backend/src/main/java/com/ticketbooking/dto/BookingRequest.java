package com.ticketbooking.dto;

import java.util.ArrayList;
import java.util.List;

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

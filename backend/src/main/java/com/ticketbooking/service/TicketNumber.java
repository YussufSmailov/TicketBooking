package com.ticketbooking.service;

public final class TicketNumber {

    private TicketNumber() {
    }

    public static String create(String type, Long bookingId) {
        String prefix = "KPL";
        if (type.equals("cinema")) {
            prefix = "CIN";
        } else if (type.equals("concert")) {
            prefix = "CON";
        }
        return prefix + "-" + String.format("%06d", bookingId);
    }
}

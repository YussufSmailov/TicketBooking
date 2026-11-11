package com.ticketbooking.dto;

import java.util.List;

public record HomeSection(String id, String title, List<HomeCard> items) {
}

package com.ticketbooking.integration;

import com.ticketbooking.repository.ConcertSeatRepository;
import com.ticketbooking.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Test
    void customerBooksFreeCinemaSeatAndSeatBecomesBooked() throws Exception {
        String body = "{\"fullName\":\"Test User\",\"email\":\"test.user@gmail.com\",\"phone\":\"+77000000000\",\"seatId\":1}";

        mockMvc.perform(post("/api/cinema/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Confirmed"));

        assertEquals("Booked", seatRepository.findById(1L).orElseThrow().getStatus());
    }

    @Test
    void bookingAlreadyTakenSeatReturnsConflict() throws Exception {
        String body = "{\"fullName\":\"Test User\",\"email\":\"test.user@gmail.com\",\"phone\":\"+77000000000\",\"seatId\":3}";

        mockMvc.perform(post("/api/cinema/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void customerBooksConcertSeat() throws Exception {
        String body = "{\"fullName\":\"Test User\",\"email\":\"test.user@gmail.com\",\"phone\":\"+77000000000\",\"seatId\":2}";

        mockMvc.perform(post("/api/concert/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertEquals("Booked", concertSeatRepository.findById(2L).orElseThrow().getStatus());
    }

    @Test
    void sportMatchesAreReturned() throws Exception {
        mockMvc.perform(get("/api/sport/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].homeTeam.name").exists());
    }
}

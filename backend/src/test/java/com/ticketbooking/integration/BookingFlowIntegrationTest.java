package com.ticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.repository.MovieSessionRepository;
import com.ticketbooking.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingFlowIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MovieSessionRepository sessionRepository;

    @Autowired
    private SeatRepository seatRepository;

    private List<Long> freeCinemaSeatIds(int sessionIndex, int count) {
        MovieSession session = sessionRepository.findAllByOrderBySessionDateAscSessionTimeAsc().get(sessionIndex);
        return seatRepository.findBySessionSessionIdOrderBySeatIdAsc(session.getSessionId())
                .stream()
                .filter(seat -> seat.getStatus().equals("Available"))
                .limit(count)
                .map(Seat::getSeatId)
                .toList();
    }

    private Long freeCinemaSeatId() {
        return freeCinemaSeatIds(0, 1).get(0);
    }

    private Long bookSeat(String token, Long seatId) throws Exception {
        MvcResult booking = mockMvc.perform(post("/api/cinema/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatId\": " + seatId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = readJson(booking);
        assertThat(json).hasSize(1);
        assertThat(json.get(0).get("status").asText()).isEqualTo("Pending");
        return json.get(0).get("bookingId").asLong();
    }

    private JsonNode pay(String token, Long bookingId, String cardNumber) throws Exception {
        String paymentBody = """
                {"newCard": {"cardNumber": "%s", "holderName": "TEST USER", "expMonth": 12, "expYear": 2030, "cvv": "123"}, "saveCard": true}
                """.formatted(cardNumber);
        MvcResult payment = mockMvc.perform(post("/api/payments/cinema/" + bookingId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(payment);
    }

    @Test
    void registerLoginBookPayAndSeeTicketInAccount() throws Exception {
        String token = registerAndLogin("Integration Tester");
        Long seatId = freeCinemaSeatId();

        Long bookingId = bookSeat(token, seatId);
        assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo("Reserved");

        JsonNode payment = pay(token, bookingId, "4242 4242 4242 4242");
        assertThat(payment.get("paymentStatus").asText()).isEqualTo("Paid");
        assertThat(payment.get("bookingStatus").asText()).isEqualTo("Confirmed");
        assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo("Booked");

        MvcResult myBookings = mockMvc.perform(get("/api/me/bookings").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tickets = readJson(myBookings);
        assertThat(tickets).hasSize(1);
        assertThat(tickets.get(0).get("bookingId").asLong()).isEqualTo(bookingId);
        assertThat(tickets.get(0).get("status").asText()).isEqualTo("Confirmed");
        assertThat(tickets.get(0).get("ticketNumber").asText()).startsWith("CIN-");

        MvcResult cards = mockMvc.perform(get("/api/me/cards").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(readJson(cards).get(0).get("title").asText()).isEqualTo("Visa •••• 4242");
    }

    @Test
    void declinedCardCancelsBookingAndFreesSeat() throws Exception {
        String token = registerAndLogin("Declined Tester");
        Long seatId = freeCinemaSeatId();
        Long bookingId = bookSeat(token, seatId);

        JsonNode payment = pay(token, bookingId, "4000 0000 0002 0000");

        assertThat(payment.get("paymentStatus").asText()).isEqualTo("Failed");
        assertThat(payment.get("bookingStatus").asText()).isEqualTo("Cancelled");
        Seat seat = seatRepository.findById(seatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo("Available");
    }

    @Test
    void userCannotCancelSomeoneElsesBooking() throws Exception {
        String ownerToken = registerAndLogin("Owner Tester");
        String strangerToken = registerAndLogin("Stranger Tester");
        Long bookingId = bookSeat(ownerToken, freeCinemaSeatId());

        mockMvc.perform(put("/api/me/bookings/cinema/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/me/bookings/cinema/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());
    }

    @Test
    void customerTokenIsForbiddenOnAdminEndpoint() throws Exception {
        String token = registerAndLogin("Customer Tester");

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void severalSeatsAreBookedAndPaidTogether() throws Exception {
        String token = registerAndLogin("Group Tester");
        List<Long> seatIds = freeCinemaSeatIds(1, 3);

        MvcResult booking = mockMvc.perform(post("/api/cinema/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("seatIds", seatIds))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode bookings = readJson(booking);
        assertThat(bookings).hasSize(3);
        List<Long> bookingIds = new java.util.ArrayList<>();
        bookings.forEach(item -> bookingIds.add(item.get("bookingId").asLong()));
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId).orElseThrow();
            assertThat(seat.getStatus()).isEqualTo("Reserved");
            expectedTotal = expectedTotal.add(seat.getPrice());
        }

        String paymentBody = """
                {"newCard": {"cardNumber": "4242 4242 4242 4242", "holderName": "TEST USER", "expMonth": 12, "expYear": 2030, "cvv": "123"},
                 "saveCard": false, "bookingIds": %s}
                """.formatted(bookingIds);
        MvcResult payment = mockMvc.perform(post("/api/payments/cinema")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode result = readJson(payment);
        assertThat(result.get("paymentStatus").asText()).isEqualTo("Paid");
        assertThat(result.get("ticketNumbers")).hasSize(3);
        assertThat(result.get("amount").decimalValue()).isEqualByComparingTo(expectedTotal);
        for (Long seatId : seatIds) {
            assertThat(seatRepository.findById(seatId).orElseThrow().getStatus()).isEqualTo("Booked");
        }

        MvcResult myBookings = mockMvc.perform(get("/api/me/bookings").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(readJson(myBookings)).hasSize(3);
    }

    @Test
    void bookingIsAllOrNothingWhenOneSeatIsTaken() throws Exception {
        String first = registerAndLogin("First Buyer");
        String second = registerAndLogin("Second Buyer");
        List<Long> seatIds = freeCinemaSeatIds(2, 2);
        bookSeat(first, seatIds.get(1));

        mockMvc.perform(post("/api/cinema/bookings")
                        .header("Authorization", "Bearer " + second)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("seatIds", seatIds))))
                .andExpect(status().isConflict());

        assertThat(seatRepository.findById(seatIds.get(0)).orElseThrow().getStatus()).isEqualTo("Available");
    }

    @Test
    void seatsFromDifferentSessionsCannotBeInOneOrder() throws Exception {
        String token = registerAndLogin("Mixed Tester");
        List<Long> seatIds = List.of(freeCinemaSeatIds(3, 1).get(0), freeCinemaSeatIds(4, 1).get(0));

        mockMvc.perform(post("/api/cinema/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("seatIds", seatIds))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tooManySeatsInOneOrderIsRejected() throws Exception {
        String token = registerAndLogin("Greedy Tester");
        List<Long> seatIds = freeCinemaSeatIds(5, 7);

        mockMvc.perform(post("/api/cinema/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("seatIds", seatIds))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unverifiedUserCannotLogIn() throws Exception {
        String email = newEmail();
        register("Lazy Tester", email);

        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"password\": \"Secret123\"}".formatted(email)))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(readJson(login).get("error").asText()).isEqualTo("EMAIL_NOT_VERIFIED");

        verifyEmail(email, sentCode(email));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"password\": \"Secret123\"}".formatted(email)))
                .andExpect(status().isOk());
    }

    @Test
    void wrongVerificationCodeIsRejected() throws Exception {
        String email = newEmail();
        register("Typo Tester", email);
        String realCode = sentCode(email);
        String wrongCode = realCode.equals("000000") ? "111111" : "000000";

        mockMvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"code\": \"%s\"}".formatted(email, wrongCode)))
                .andExpect(status().isBadRequest());
        verifyEmail(email, realCode);
    }
}

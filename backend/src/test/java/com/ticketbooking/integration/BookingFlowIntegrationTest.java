package com.ticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.repository.MovieSessionRepository;
import com.ticketbooking.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieSessionRepository sessionRepository;

    @Autowired
    private SeatRepository seatRepository;

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String registerAndLogin(String fullName) throws Exception {
        String email = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@mail.kz";
        String registerBody = """
                {"fullName": "%s", "email": "%s", "phone": "+77010001122", "password": "Secret123"}
                """.formatted(fullName, email);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {"email": "%s", "password": "Secret123"}
                """.formatted(email);
        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(login).get("token").asText();
    }

    private Long freeCinemaSeatId() {
        MovieSession session = sessionRepository.findAllByOrderBySessionDateAscSessionTimeAsc().get(0);
        return seatRepository.findBySessionSessionIdOrderBySeatIdAsc(session.getSessionId())
                .stream()
                .filter(seat -> seat.getStatus().equals("Available"))
                .findFirst()
                .orElseThrow()
                .getSeatId();
    }

    private Long bookSeat(String token, Long seatId) throws Exception {
        MvcResult booking = mockMvc.perform(post("/api/cinema/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatId\": " + seatId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = readJson(booking);
        assertThat(json.get("status").asText()).isEqualTo("Pending");
        return json.get("bookingId").asLong();
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
}

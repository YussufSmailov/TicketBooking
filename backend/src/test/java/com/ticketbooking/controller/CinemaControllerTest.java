package com.ticketbooking.controller;

import com.ticketbooking.service.CinemaService;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CinemaController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CinemaService cinemaService;

    @Test
    void movieWithZeroDurationReturns400() throws Exception {
        String body = """
                {"title": "Dune", "durationMin": 0, "ageRating": "16+"}
                """;

        mockMvc.perform(post("/api/cinema/movies").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.durationMin").exists());
    }

    @Test
    void sessionInThePastReturns400() throws Exception {
        String body = """
                {"movieId": 1, "sessionDate": "2020-01-01", "sessionTime": "18:00", "hallNumber": 1}
                """;

        mockMvc.perform(post("/api/cinema/sessions").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sessionDate").exists());
    }

    @Test
    void seatRowWithNegativePriceReturns400() throws Exception {
        String body = """
                {"rowNumber": 1, "seatCount": 10, "price": -100}
                """;

        mockMvc.perform(post("/api/cinema/sessions/1/seats").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }
}

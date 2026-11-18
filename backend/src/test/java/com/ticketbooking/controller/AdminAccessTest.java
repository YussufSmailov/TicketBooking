package com.ticketbooking.controller;

import com.ticketbooking.entity.Movie;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.security.SecurityConfig;
import com.ticketbooking.service.CinemaService;
import com.ticketbooking.service.ConcertService;
import com.ticketbooking.service.SportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CinemaController.class, ConcertController.class, SportController.class})
@Import(SecurityConfig.class)
class AdminAccessTest {

    private static final String MOVIE_JSON = """
            {"title": "Dune: Part Three", "durationMin": 160, "ageRating": "16+"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CinemaService cinemaService;

    @MockBean
    private ConcertService concertService;

    @MockBean
    private SportService sportService;

    @Test
    void createMovieWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/cinema/movies").contentType(MediaType.APPLICATION_JSON).content(MOVIE_JSON))
                .andExpect(status().isUnauthorized());

        verify(cinemaService, never()).createMovie(any());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotCreateMovie() throws Exception {
        mockMvc.perform(post("/api/cinema/movies").contentType(MediaType.APPLICATION_JSON).content(MOVIE_JSON))
                .andExpect(status().isForbidden());

        verify(cinemaService, never()).createMovie(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateMovie() throws Exception {
        Movie movie = new Movie();
        movie.setMovieId(1L);
        when(cinemaService.createMovie(any())).thenReturn(movie);

        mockMvc.perform(post("/api/cinema/movies").contentType(MediaType.APPLICATION_JSON).content(MOVIE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotDeleteConcertOrMatch() throws Exception {
        mockMvc.perform(delete("/api/concert/concerts/1")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/sport/matches/1")).andExpect(status().isForbidden());
    }

    @Test
    void eventListsArePublic() throws Exception {
        when(cinemaService.searchMovies(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/cinema/movies")).andExpect(status().isOk());
        mockMvc.perform(get("/api/concert/concerts")).andExpect(status().isOk());
        mockMvc.perform(get("/api/sport/matches/1/seats")).andExpect(status().isOk());
    }

    @Test
    void bookingWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/sport/bookings").contentType(MediaType.APPLICATION_JSON).content("{\"seatId\": 1}"))
                .andExpect(status().isUnauthorized());
    }
}

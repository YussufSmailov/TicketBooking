package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.entity.Movie;
import com.ticketbooking.service.CinemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CinemaController.class)
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CinemaService cinemaService;

    @Test
    void getMoviesReturnsMovieList() throws Exception {
        Movie movie = new Movie();
        movie.setMovieId(1L);
        movie.setTitle("Dune: Part Three");
        movie.setDurationMin(160);
        movie.setAgeRating("16+");

        when(cinemaService.getAllMovies()).thenReturn(List.of(movie));

        mockMvc.perform(get("/api/cinema/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Dune: Part Three"))
                .andExpect(jsonPath("$[0].durationMin").value(160));
    }

    @Test
    void createMovieReturnsCreated() throws Exception {
        Movie savedMovie = new Movie();
        savedMovie.setMovieId(4L);
        savedMovie.setTitle("New Movie");
        savedMovie.setDurationMin(120);
        savedMovie.setAgeRating("6+");

        when(cinemaService.createMovie(any(Movie.class))).thenReturn(savedMovie);

        String body = "{\"title\":\"New Movie\",\"durationMin\":120,\"ageRating\":\"6+\"}";

        mockMvc.perform(post("/api/cinema/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieId").value(4));
    }

    @Test
    void bookingTakenSeatReturnsConflict() throws Exception {
        when(cinemaService.createBooking(any(BookingRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Seat is already taken"));

        String body = "{\"fullName\":\"Amina Seitkali\",\"email\":\"amina@gmail.com\",\"phone\":\"+77011234567\",\"seatId\":3}";

        mockMvc.perform(post("/api/cinema/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Seat is already taken"));
    }
}

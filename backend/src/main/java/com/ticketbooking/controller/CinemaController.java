package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.MovieFilter;
import com.ticketbooking.dto.MovieRequest;
import com.ticketbooking.dto.SeatRowRequest;
import com.ticketbooking.dto.SessionRequest;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.Movie;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.service.CinemaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cinema")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping("/movies")
    public List<Movie> getMovies(@ModelAttribute MovieFilter filter) {
        return cinemaService.searchMovies(filter);
    }

    @GetMapping("/movies/{id}")
    public Movie getMovie(@PathVariable Long id) {
        return cinemaService.getMovieById(id);
    }

    @GetMapping("/movies/{id}/sessions")
    public List<MovieSession> getMovieSessions(@PathVariable Long id) {
        return cinemaService.getSessionsByMovie(id);
    }

    @PostMapping("/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(@Valid @RequestBody MovieRequest request) {
        return cinemaService.createMovie(request);
    }

    @PutMapping("/movies/{id}")
    public Movie updateMovie(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return cinemaService.updateMovie(id, request);
    }

    @DeleteMapping("/movies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable Long id) {
        cinemaService.deleteMovie(id);
    }

    @GetMapping("/sessions")
    public List<MovieSession> getAllSessions() {
        return cinemaService.getAllSessions();
    }

    @GetMapping("/sessions/{id}")
    public MovieSession getSession(@PathVariable Long id) {
        return cinemaService.getSessionById(id);
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieSession createSession(@Valid @RequestBody SessionRequest request) {
        return cinemaService.createSession(request);
    }

    @PutMapping("/sessions/{id}")
    public MovieSession updateSession(@PathVariable Long id, @Valid @RequestBody SessionRequest request) {
        return cinemaService.updateSession(id, request);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long id) {
        cinemaService.deleteSession(id);
    }

    @GetMapping("/sessions/{id}/seats")
    public List<Seat> getSeats(@PathVariable Long id) {
        return cinemaService.getSeatsBySession(id);
    }

    @PostMapping("/sessions/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Seat> addSeatRow(@PathVariable Long id, @Valid @RequestBody SeatRowRequest request) {
        return cinemaService.addSeatRow(id, request);
    }

    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return cinemaService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Booking> createBooking(@RequestBody BookingRequest request, Principal principal) {
        return cinemaService.createBookings(request.allSeatIds(), principal.getName());
    }

    @PutMapping("/bookings/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return cinemaService.cancelBooking(id);
    }
}

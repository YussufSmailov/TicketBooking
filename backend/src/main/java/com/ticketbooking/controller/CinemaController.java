package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.SeatGroupRequest;
import com.ticketbooking.dto.SessionRequest;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.Movie;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.service.CinemaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinema")
@CrossOrigin(origins = "*")
public class CinemaController {

    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @GetMapping("/movies")
    public List<Movie> getAllMovies() {
        return cinemaService.getAllMovies();
    }

    @GetMapping("/movies/{id}")
    public Movie getMovie(@PathVariable Long id) {
        return cinemaService.getMovieById(id);
    }

    @PostMapping("/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(@RequestBody Movie movie) {
        return cinemaService.createMovie(movie);
    }

    @PutMapping("/movies/{id}")
    public Movie updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        return cinemaService.updateMovie(id, movie);
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

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieSession createSession(@RequestBody SessionRequest request) {
        return cinemaService.createSession(request);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long id) {
        cinemaService.deleteSession(id);
    }

    @PostMapping("/sessions/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Seat> addSeats(@PathVariable Long id, @RequestBody SeatGroupRequest request) {
        return cinemaService.addSeats(id, request);
    }

    @GetMapping("/sessions/{id}/seats")
    public List<Seat> getSeats(@PathVariable Long id) {
        return cinemaService.getSeatsBySession(id);
    }

    @GetMapping("/bookings")
    public List<Booking> getAllBookings() {
        return cinemaService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@RequestBody BookingRequest request) {
        return cinemaService.createBooking(request);
    }

    @PutMapping("/bookings/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return cinemaService.cancelBooking(id);
    }
}

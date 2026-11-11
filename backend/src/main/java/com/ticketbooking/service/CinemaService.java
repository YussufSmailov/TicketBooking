package com.ticketbooking.service;

import com.ticketbooking.dto.MovieRequest;
import com.ticketbooking.dto.SeatRowRequest;
import com.ticketbooking.dto.SessionRequest;
import com.ticketbooking.entity.*;
import com.ticketbooking.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CinemaService {

    private final MovieRepository movieRepository;
    private final MovieSessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    public CinemaService(MovieRepository movieRepository,
                         MovieSessionRepository sessionRepository,
                         SeatRepository seatRepository,
                         BookingRepository bookingRepository,
                         UserService userService) {
        this.movieRepository = movieRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм не найден"));
    }

    public Movie createMovie(MovieRequest request) {
        Movie movie = new Movie();
        copyMovieFields(request, movie);
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Long id, MovieRequest request) {
        Movie movie = getMovieById(id);
        copyMovieFields(request, movie);
        return movieRepository.save(movie);
    }

    private void copyMovieFields(MovieRequest request, Movie movie) {
        movie.setTitle(request.title().trim());
        movie.setDurationMin(request.durationMin());
        movie.setAgeRating(request.ageRating());

        MovieDetails details = movie.getDetails();
        if (details == null) {
            details = new MovieDetails();
            details.setMovie(movie);
            movie.setDetails(details);
        }
        details.setDescription(request.description());
        details.setPosterUrl(emptyToNull(request.posterUrl()));
        details.setGenre(request.genre());
        details.setReleaseDate(request.releaseDate());
        details.setPremiere(request.premiere());
    }

    private String emptyToNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }

    public List<MovieSession> getAllSessions() {
        return sessionRepository.findAllByOrderBySessionDateAscSessionTimeAsc();
    }

    public List<MovieSession> getSessionsByMovie(Long movieId) {
        getMovieById(movieId);
        return sessionRepository.findByMovieMovieIdOrderBySessionDateAscSessionTimeAsc(movieId);
    }

    public MovieSession getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сеанс не найден"));
    }

    public MovieSession createSession(SessionRequest request) {
        MovieSession session = new MovieSession();
        copySessionFields(request, session);
        return sessionRepository.save(session);
    }

    public MovieSession updateSession(Long id, SessionRequest request) {
        MovieSession session = getSessionById(id);
        copySessionFields(request, session);
        return sessionRepository.save(session);
    }

    private void copySessionFields(SessionRequest request, MovieSession session) {
        session.setMovie(getMovieById(request.movieId()));
        session.setSessionDate(request.sessionDate());
        session.setSessionTime(request.sessionTime());
        session.setHallNumber(request.hallNumber());
    }

    public void deleteSession(Long id) {
        MovieSession session = getSessionById(id);
        sessionRepository.delete(session);
    }

    public List<Seat> getSeatsBySession(Long sessionId) {
        return seatRepository.findBySessionSessionIdOrderBySeatIdAsc(sessionId);
    }

    @Transactional
    public List<Seat> addSeatRow(Long sessionId, SeatRowRequest request) {
        MovieSession session = getSessionById(sessionId);
        String firstSeatNumber = request.rowNumber() + "-1";
        if (seatRepository.existsBySessionSessionIdAndSeatNumber(sessionId, firstSeatNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ряд " + request.rowNumber() + " уже есть на этом сеансе");
        }

        List<Seat> newSeats = new ArrayList<>();
        for (int number = 1; number <= request.seatCount(); number++) {
            Seat seat = new Seat();
            seat.setSession(session);
            seat.setSeatNumber(request.rowNumber() + "-" + number);
            seat.setPrice(request.price());
            seat.setStatus("Available");
            newSeats.add(seat);
        }
        return seatRepository.saveAll(newSeats);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking createBooking(Long seatId, String userEmail) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено"));

        if (!seat.getStatus().equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это место уже занято");
        }

        User user = userService.getByEmail(userEmail);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Pending");

        seat.setStatus("Reserved");
        seatRepository.save(seat);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        booking.setStatus("Cancelled");

        Seat seat = booking.getSeat();
        seat.setStatus("Available");
        seatRepository.save(seat);

        return bookingRepository.save(booking);
    }
}

package com.ticketbooking.service;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.SeatGroupRequest;
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
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public CinemaService(MovieRepository movieRepository,
                         MovieSessionRepository sessionRepository,
                         SeatRepository seatRepository,
                         BookingRepository bookingRepository,
                         PaymentRepository paymentRepository,
                         UserService userService) {
        this.movieRepository = movieRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм не найден"));
    }

    public Movie createMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Long id, Movie newData) {
        Movie movie = getMovieById(id);
        movie.setTitle(newData.getTitle());
        movie.setDurationMin(newData.getDurationMin());
        movie.setAgeRating(newData.getAgeRating());
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        movieRepository.delete(movie);
    }

    public List<MovieSession> getAllSessions() {
        return sessionRepository.findAllByOrderBySessionDateAscSessionTimeAsc();
    }

    public MovieSession getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Сеанс не найден"));
    }

    public MovieSession createSession(SessionRequest request) {
        if (request.movieId() == null || request.sessionDate() == null || request.sessionTime() == null || request.hallNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Выберите фильм и заполните дату, время и номер зала");
        }

        MovieSession session = new MovieSession();
        session.setMovie(getMovieById(request.movieId()));
        session.setSessionDate(request.sessionDate());
        session.setSessionTime(request.sessionTime());
        session.setHallNumber(request.hallNumber());
        return sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(Long id) {
        MovieSession session = getSessionById(id);
        List<Seat> seats = seatRepository.findBySessionSessionIdOrderBySeatIdAsc(id);
        seatRepository.deleteAll(seats);
        seatRepository.flush();
        sessionRepository.delete(session);
    }

    @Transactional
    public List<Seat> addSeats(Long sessionId, SeatGroupRequest request) {
        request.validate();
        MovieSession session = getSessionById(sessionId);
        String row = request.name().trim().toUpperCase();

        List<Seat> newSeats = new ArrayList<>();
        for (int i = 1; i <= request.count(); i++) {
            Seat seat = new Seat();
            seat.setSession(session);
            seat.setSeatNumber(row + i);
            seat.setPrice(request.price());
            seat.setStatus("Available");
            newSeats.add(seat);
        }
        return seatRepository.saveAllAndFlush(newSeats);
    }

    public List<Seat> getSeatsBySession(Long sessionId) {
        return seatRepository.findBySessionSessionIdOrderBySeatIdAsc(sessionId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        Seat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено"));

        if (!seat.getStatus().equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это место уже занято");
        }

        User user = userService.findOrCreate(request.fullName(), request.email(), request.phone());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Pending");
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(seat.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("Paid");
        paymentRepository.save(payment);

        seat.setStatus("Booked");
        seatRepository.save(seat);

        booking.setStatus("Confirmed");
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

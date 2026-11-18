package com.ticketbooking.service;

import com.ticketbooking.dto.MovieFilter;
import com.ticketbooking.dto.MovieRequest;
import com.ticketbooking.dto.SeatRowRequest;
import com.ticketbooking.dto.SessionRequest;
import com.ticketbooking.entity.*;
import com.ticketbooking.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CinemaService {

    private final MovieRepository movieRepository;
    private final MovieSessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ReviewService reviewService;

    @Value("${app.booking.max-seats:6}")
    private int maxSeats = 6;

    public CinemaService(MovieRepository movieRepository,
                         MovieSessionRepository sessionRepository,
                         SeatRepository seatRepository,
                         BookingRepository bookingRepository,
                         UserService userService,
                         ReviewService reviewService) {
        this.movieRepository = movieRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> searchMovies(MovieFilter filter) {
        Set<Long> moviesOnDate = null;
        if (filter.date() != null) {
            moviesOnDate = sessionRepository.findBySessionDate(filter.date()).stream()
                    .map(session -> session.getMovie().getMovieId())
                    .collect(Collectors.toSet());
        }

        List<Movie> result = new ArrayList<>();
        for (Movie movie : movieRepository.findAll()) {
            MovieDetails details = movie.getDetails();
            String genre = details != null ? details.getGenre() : null;
            String description = details != null ? details.getDescription() : null;

            if (!TextSearch.matches(filter.q(), movie.getTitle(), genre, description)) continue;
            if (!TextSearch.sameText(filter.genre(), genre)) continue;
            if (!TextSearch.sameText(filter.ageRating(), movie.getAgeRating())) continue;
            if (filter.minRating() != null && (movie.getRating() == null || movie.getRating() < filter.minRating())) continue;
            if (filter.minDuration() != null && movie.getDurationMin() < filter.minDuration()) continue;
            if (filter.maxDuration() != null && movie.getDurationMin() > filter.maxDuration()) continue;
            if (moviesOnDate != null && !moviesOnDate.contains(movie.getMovieId())) continue;
            result.add(movie);
        }

        result.sort(movieOrder(filter.sort()));
        return result;
    }

    private Comparator<Movie> movieOrder(String sort) {
        Comparator<Movie> byTitle = Comparator.comparing(Movie::getTitle, String.CASE_INSENSITIVE_ORDER);
        if (sort == null || sort.isBlank() || sort.equals("rating")) {
            return Comparator.comparing(Movie::getRating, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(byTitle);
        }
        return switch (sort) {
            case "title" -> byTitle;
            case "duration" -> Comparator.comparing(Movie::getDurationMin).thenComparing(byTitle);
            case "durationDesc" -> Comparator.comparing(Movie::getDurationMin, Comparator.reverseOrder()).thenComparing(byTitle);
            case "release" -> Comparator.comparing((Movie movie) -> movie.getDetails() != null ? movie.getDetails().getReleaseDate() : null,
                    Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(byTitle);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестная сортировка: " + sort);
        };
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

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = getMovieById(id);
        reviewService.deleteReviewsOf("movie", id);
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
        return createBookings(List.of(seatId), userEmail).get(0);
    }

    /**
     * Бронирует сразу несколько мест одного сеанса. Если хоть одно место занято, не бронируется ни одно.
     */
    @Transactional
    public List<Booking> createBookings(List<Long> seatIds, String userEmail) {
        SeatSelection.check(seatIds, maxSeats);
        List<Seat> seats = seatRepository.findAllById(seatIds);
        SeatSelection.checkAllFound(seatIds.size(), seats.size());

        Set<Long> sessions = new HashSet<>();
        for (Seat seat : seats) {
            sessions.add(seat.getSession().getSessionId());
            SeatSelection.checkAvailable(seat.getStatus());
        }
        SeatSelection.checkSameEvent(sessions);

        User user = userService.getByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = new ArrayList<>();
        for (Seat seat : seats) {
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSeat(seat);
            booking.setBookingDate(now);
            booking.setStatus("Pending");
            seat.setStatus("Reserved");
            bookings.add(booking);
        }
        seatRepository.saveAll(seats);
        return bookingRepository.saveAll(bookings);
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

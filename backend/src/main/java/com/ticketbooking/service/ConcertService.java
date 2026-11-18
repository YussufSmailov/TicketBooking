package com.ticketbooking.service;

import com.ticketbooking.dto.ConcertFilter;
import com.ticketbooking.dto.ConcertRequest;
import com.ticketbooking.dto.ZoneSeatRowRequest;
import com.ticketbooking.entity.Concert;
import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.ConcertSeat;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.ConcertBookingRepository;
import com.ticketbooking.repository.ConcertRepository;
import com.ticketbooking.repository.ConcertSeatRepository;
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

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertSeatRepository seatRepository;
    private final ConcertBookingRepository bookingRepository;
    private final UserService userService;
    private final ReviewService reviewService;

    @Value("${app.booking.max-seats:6}")
    private int maxSeats = 6;

    public ConcertService(ConcertRepository concertRepository,
                          ConcertSeatRepository seatRepository,
                          ConcertBookingRepository bookingRepository,
                          UserService userService,
                          ReviewService reviewService) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.reviewService = reviewService;
    }

    public List<Concert> getAllConcerts() {
        return concertRepository.findAllByOrderByConcertDateAsc();
    }

    public List<Concert> searchConcerts(ConcertFilter filter) {
        List<Concert> result = new ArrayList<>();
        for (Concert concert : concertRepository.findAllByOrderByConcertDateAsc()) {
            if (!TextSearch.matches(filter.q(), concert.getArtist(), concert.getTitle(), concert.getVenue(),
                    concert.getCity(), concert.getGenre(), concert.getDescription())) continue;
            if (!TextSearch.sameText(filter.city(), concert.getCity())) continue;
            if (!TextSearch.sameText(filter.genre(), concert.getGenre())) continue;
            if (filter.minRating() != null && (concert.getRating() == null || concert.getRating() < filter.minRating())) continue;
            if (filter.minDuration() != null && (concert.getDurationMin() == null || concert.getDurationMin() < filter.minDuration())) continue;
            if (filter.maxDuration() != null && (concert.getDurationMin() == null || concert.getDurationMin() > filter.maxDuration())) continue;
            if (filter.dateFrom() != null && concert.getConcertDate().isBefore(filter.dateFrom())) continue;
            if (filter.dateTo() != null && concert.getConcertDate().isAfter(filter.dateTo())) continue;
            result.add(concert);
        }
        result.sort(concertOrder(filter.sort()));
        return result;
    }

    private Comparator<Concert> concertOrder(String sort) {
        Comparator<Concert> byDate = Comparator.comparing(Concert::getConcertDate).thenComparing(Concert::getConcertTime);
        if (sort == null || sort.isBlank() || sort.equals("date")) {
            return byDate;
        }
        return switch (sort) {
            case "rating" -> Comparator.comparing(Concert::getRating, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(byDate);
            case "duration" -> Comparator.comparing(Concert::getDurationMin, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(byDate);
            case "durationDesc" -> Comparator.comparing(Concert::getDurationMin, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(byDate);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестная сортировка: " + sort);
        };
    }

    public Concert getConcertById(Long id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Концерт не найден"));
    }

    public Concert createConcert(ConcertRequest request) {
        Concert concert = new Concert();
        copyConcertFields(request, concert);
        return concertRepository.save(concert);
    }

    public Concert updateConcert(Long id, ConcertRequest request) {
        Concert concert = getConcertById(id);
        copyConcertFields(request, concert);
        return concertRepository.save(concert);
    }

    private void copyConcertFields(ConcertRequest request, Concert concert) {
        concert.setArtist(request.artist().trim());
        concert.setTitle(request.title().trim());
        concert.setVenue(request.venue().trim());
        concert.setCity(request.city().trim());
        concert.setConcertDate(request.concertDate());
        concert.setConcertTime(request.concertTime());
        concert.setDurationMin(request.durationMin());
        concert.setGenre(request.genre() == null || request.genre().isBlank() ? null : request.genre().trim());
        concert.setDescription(request.description());
        concert.setPosterUrl(request.posterUrl() == null || request.posterUrl().isBlank() ? null : request.posterUrl().trim());
    }

    @Transactional
    public void deleteConcert(Long id) {
        Concert concert = getConcertById(id);
        reviewService.deleteReviewsOf("concert", id);
        concertRepository.delete(concert);
    }

    public List<ConcertSeat> getSeatsByConcert(Long concertId) {
        return seatRepository.findByConcertConcertIdOrderBySeatIdAsc(concertId);
    }

    @Transactional
    public List<ConcertSeat> addSeatRow(Long concertId, ZoneSeatRowRequest request) {
        Concert concert = getConcertById(concertId);
        String zone = request.zone().trim();
        String firstSeatNumber = request.rowNumber() + "-1";
        if (seatRepository.existsByConcertConcertIdAndZoneAndSeatNumber(concertId, zone, firstSeatNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ряд " + request.rowNumber() + " уже есть в зоне " + zone);
        }

        List<ConcertSeat> newSeats = new ArrayList<>();
        for (int number = 1; number <= request.seatCount(); number++) {
            ConcertSeat seat = new ConcertSeat();
            seat.setConcert(concert);
            seat.setZone(zone);
            seat.setSeatNumber(request.rowNumber() + "-" + number);
            seat.setPrice(request.price());
            seat.setStatus("Available");
            newSeats.add(seat);
        }
        return seatRepository.saveAll(newSeats);
    }

    public List<ConcertBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public ConcertBooking createBooking(Long seatId, String userEmail) {
        return createBookings(List.of(seatId), userEmail).get(0);
    }

    /**
     * Бронирует сразу несколько мест одного концерта. Если хоть одно место занято, не бронируется ни одно.
     */
    @Transactional
    public List<ConcertBooking> createBookings(List<Long> seatIds, String userEmail) {
        SeatSelection.check(seatIds, maxSeats);
        List<ConcertSeat> seats = seatRepository.findAllById(seatIds);
        SeatSelection.checkAllFound(seatIds.size(), seats.size());

        Set<Long> events = new HashSet<>();
        for (ConcertSeat seat : seats) {
            events.add(seat.getConcert().getConcertId());
            SeatSelection.checkAvailable(seat.getStatus());
        }
        SeatSelection.checkSameEvent(events);

        User user = userService.getByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        List<ConcertBooking> bookings = new ArrayList<>();
        for (ConcertSeat seat : seats) {
            ConcertBooking booking = new ConcertBooking();
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
    public ConcertBooking cancelBooking(Long bookingId) {
        ConcertBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        booking.setStatus("Cancelled");

        ConcertSeat seat = booking.getSeat();
        seat.setStatus("Available");
        seatRepository.save(seat);

        return bookingRepository.save(booking);
    }
}

package com.ticketbooking.service;

import com.ticketbooking.dto.ConcertRequest;
import com.ticketbooking.dto.ZoneSeatRowRequest;
import com.ticketbooking.entity.Concert;
import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.ConcertSeat;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.ConcertBookingRepository;
import com.ticketbooking.repository.ConcertRepository;
import com.ticketbooking.repository.ConcertSeatRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final ConcertSeatRepository seatRepository;
    private final ConcertBookingRepository bookingRepository;
    private final UserService userService;

    public ConcertService(ConcertRepository concertRepository,
                          ConcertSeatRepository seatRepository,
                          ConcertBookingRepository bookingRepository,
                          UserService userService) {
        this.concertRepository = concertRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    public List<Concert> getAllConcerts() {
        return concertRepository.findAllByOrderByConcertDateAsc();
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
        concert.setDescription(request.description());
        concert.setPosterUrl(request.posterUrl() == null || request.posterUrl().isBlank() ? null : request.posterUrl().trim());
    }

    public void deleteConcert(Long id) {
        Concert concert = getConcertById(id);
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
        ConcertSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено"));

        if (!seat.getStatus().equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это место уже занято");
        }

        User user = userService.getByEmail(userEmail);

        ConcertBooking booking = new ConcertBooking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Pending");

        seat.setStatus("Reserved");
        seatRepository.save(seat);

        return bookingRepository.save(booking);
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

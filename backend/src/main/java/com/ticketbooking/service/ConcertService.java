package com.ticketbooking.service;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.SeatGroupRequest;
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

    public Concert createConcert(Concert concert) {
        return concertRepository.save(concert);
    }

    public Concert updateConcert(Long id, Concert newData) {
        Concert concert = getConcertById(id);
        concert.setArtist(newData.getArtist());
        concert.setTitle(newData.getTitle());
        concert.setVenue(newData.getVenue());
        concert.setCity(newData.getCity());
        concert.setConcertDate(newData.getConcertDate());
        concert.setConcertTime(newData.getConcertTime());
        return concertRepository.save(concert);
    }

    @Transactional
    public void deleteConcert(Long id) {
        Concert concert = getConcertById(id);
        List<ConcertSeat> seats = seatRepository.findByConcertConcertIdOrderBySeatIdAsc(id);
        seatRepository.deleteAll(seats);
        seatRepository.flush();
        concertRepository.delete(concert);
    }

    @Transactional
    public List<ConcertSeat> addSeats(Long concertId, SeatGroupRequest request) {
        request.validate();
        Concert concert = getConcertById(concertId);
        String zone = request.name().trim();
        String prefix = zone.substring(0, 1).toUpperCase();
        int existingSeats = seatRepository.findByConcertConcertIdOrderBySeatIdAsc(concertId).size();

        List<ConcertSeat> newSeats = new ArrayList<>();
        for (int i = 1; i <= request.count(); i++) {
            ConcertSeat seat = new ConcertSeat();
            seat.setConcert(concert);
            seat.setZone(zone);
            seat.setSeatNumber(prefix + "-" + (existingSeats + i));
            seat.setPrice(request.price());
            seat.setStatus("Available");
            newSeats.add(seat);
        }
        return seatRepository.saveAllAndFlush(newSeats);
    }

    public List<ConcertSeat> getSeatsByConcert(Long concertId) {
        return seatRepository.findByConcertConcertIdOrderBySeatIdAsc(concertId);
    }

    public List<ConcertBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public ConcertBooking createBooking(BookingRequest request) {
        ConcertSeat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено"));

        if (!seat.getStatus().equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это место уже занято");
        }

        User user = userService.findOrCreate(request.fullName(), request.email(), request.phone());

        ConcertBooking booking = new ConcertBooking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Confirmed");

        seat.setStatus("Booked");
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

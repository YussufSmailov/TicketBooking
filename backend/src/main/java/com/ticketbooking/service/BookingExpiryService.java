package com.ticketbooking.service;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.MatchBooking;
import com.ticketbooking.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ConcertBookingRepository concertBookingRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final MatchBookingRepository matchBookingRepository;
    private final MatchSeatRepository matchSeatRepository;

    @Value("${app.booking.reservation-minutes:10}")
    private long reservationMinutes = 10;

    public BookingExpiryService(BookingRepository bookingRepository,
                                SeatRepository seatRepository,
                                ConcertBookingRepository concertBookingRepository,
                                ConcertSeatRepository concertSeatRepository,
                                MatchBookingRepository matchBookingRepository,
                                MatchSeatRepository matchSeatRepository) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.concertBookingRepository = concertBookingRepository;
        this.concertSeatRepository = concertSeatRepository;
        this.matchBookingRepository = matchBookingRepository;
        this.matchSeatRepository = matchSeatRepository;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public int releaseExpiredBookings() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(reservationMinutes);
        int released = 0;

        List<Booking> cinemaBookings = bookingRepository.findByStatusAndBookingDateBefore("Pending", deadline);
        for (Booking booking : cinemaBookings) {
            booking.setStatus("Cancelled");
            booking.getSeat().setStatus("Available");
            seatRepository.save(booking.getSeat());
            bookingRepository.save(booking);
            released++;
        }

        List<ConcertBooking> concertBookings = concertBookingRepository.findByStatusAndBookingDateBefore("Pending", deadline);
        for (ConcertBooking booking : concertBookings) {
            booking.setStatus("Cancelled");
            booking.getSeat().setStatus("Available");
            concertSeatRepository.save(booking.getSeat());
            concertBookingRepository.save(booking);
            released++;
        }

        List<MatchBooking> matchBookings = matchBookingRepository.findByStatusAndBookingDateBefore("Pending", deadline);
        for (MatchBooking booking : matchBookings) {
            booking.setStatus("Cancelled");
            booking.getSeat().setStatus("Available");
            matchSeatRepository.save(booking.getSeat());
            matchBookingRepository.save(booking);
            released++;
        }

        return released;
    }
}

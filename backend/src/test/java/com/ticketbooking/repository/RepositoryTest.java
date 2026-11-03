package com.ticketbooking.repository;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieSessionRepository sessionRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void findByEmailReturnsUser() {
        Optional<User> user = userRepository.findByEmail("amina@gmail.com");

        assertTrue(user.isPresent());
        assertEquals("Amina Seitkali", user.get().getFullName());
    }

    @Test
    void findSeatsBySessionReturnsAllSeatsInOrder() {
        List<Seat> seats = seatRepository.findBySessionSessionIdOrderBySeatIdAsc(1L);

        assertEquals(40, seats.size());
        assertEquals("A1", seats.get(0).getSeatNumber());
    }

    @Test
    void databaseRejectsSecondActiveBookingForSameSeat() {
        User user = userRepository.findByEmail("arman@mail.kz").orElseThrow();
        Seat bookedSeat = seatRepository.findById(3L).orElseThrow();

        Booking secondBooking = new Booking();
        secondBooking.setUser(user);
        secondBooking.setSeat(bookedSeat);
        secondBooking.setBookingDate(LocalDateTime.now());
        secondBooking.setStatus("Pending");

        assertThrows(DataIntegrityViolationException.class,
                () -> bookingRepository.saveAndFlush(secondBooking));
    }

    @Test
    void databaseRejectsNegativeSeatPrice() {
        MovieSession session = sessionRepository.findById(1L).orElseThrow();

        Seat seat = new Seat();
        seat.setSession(session);
        seat.setSeatNumber("Z1");
        seat.setPrice(new BigDecimal("-100"));
        seat.setStatus("Available");

        assertThrows(DataIntegrityViolationException.class,
                () -> seatRepository.saveAndFlush(seat));
    }
}

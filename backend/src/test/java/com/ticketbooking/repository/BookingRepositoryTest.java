package com.ticketbooking.repository;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private MovieSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    private List<Seat> freeSeats(int count) {
        MovieSession session = sessionRepository.findAllByOrderBySessionDateAscSessionTimeAsc().get(0);
        return seatRepository.findBySessionSessionIdOrderBySeatIdAsc(session.getSessionId())
                .stream()
                .filter(seat -> seat.getStatus().equals("Available"))
                .limit(count)
                .toList();
    }

    private Booking saveBooking(User user, Seat seat, String status, LocalDateTime date) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setStatus(status);
        booking.setBookingDate(date);
        return bookingRepository.save(booking);
    }

    @Test
    void findsOnlyBookingsOfThisUserNewestFirst() {
        User user = new User();
        user.setFullName("Repo Tester");
        user.setEmail("repo.tester@mail.kz");
        user.setPhone("+77010000002");
        user = userRepository.save(user);
        List<Seat> seats = freeSeats(2);
        Booking older = saveBooking(user, seats.get(0), "Confirmed", LocalDateTime.now().minusDays(1));
        Booking newer = saveBooking(user, seats.get(1), "Pending", LocalDateTime.now());

        List<Booking> found = bookingRepository.findByUserUserIdOrderByBookingDateDesc(user.getUserId());

        assertThat(found).extracting(Booking::getBookingId).containsExactly(newer.getBookingId(), older.getBookingId());
    }

    @Test
    void findsOnlyPendingBookingsOlderThanDeadline() {
        User user = userRepository.findByEmail("amina@gmail.com").orElseThrow();
        List<Seat> seats = freeSeats(3);
        Booking expired = saveBooking(user, seats.get(0), "Pending", LocalDateTime.now().minusMinutes(15));
        Booking fresh = saveBooking(user, seats.get(1), "Pending", LocalDateTime.now().minusMinutes(2));
        Booking paid = saveBooking(user, seats.get(2), "Confirmed", LocalDateTime.now().minusMinutes(30));

        List<Booking> found = bookingRepository.findByStatusAndBookingDateBefore("Pending", LocalDateTime.now().minusMinutes(10));

        assertThat(found).extracting(Booking::getBookingId)
                .contains(expired.getBookingId())
                .doesNotContain(fresh.getBookingId(), paid.getBookingId());
    }
}

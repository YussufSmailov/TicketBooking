package com.ticketbooking.unit;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.MatchBooking;
import com.ticketbooking.entity.MatchSeat;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.BookingExpiryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingExpiryServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ConcertBookingRepository concertBookingRepository;
    @Mock
    private ConcertSeatRepository concertSeatRepository;
    @Mock
    private MatchBookingRepository matchBookingRepository;
    @Mock
    private MatchSeatRepository matchSeatRepository;

    @InjectMocks
    private BookingExpiryService expiryService;

    @Test
    void expiredPendingBookingsAreCancelledAndSeatsReleased() {
        Seat cinemaSeat = new Seat();
        cinemaSeat.setStatus("Reserved");
        Booking cinemaBooking = new Booking();
        cinemaBooking.setStatus("Pending");
        cinemaBooking.setSeat(cinemaSeat);

        MatchSeat matchSeat = new MatchSeat();
        matchSeat.setStatus("Reserved");
        MatchBooking matchBooking = new MatchBooking();
        matchBooking.setStatus("Pending");
        matchBooking.setSeat(matchSeat);

        when(bookingRepository.findByStatusAndBookingDateBefore(eq("Pending"), any(LocalDateTime.class)))
                .thenReturn(List.of(cinemaBooking));
        when(concertBookingRepository.findByStatusAndBookingDateBefore(eq("Pending"), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(matchBookingRepository.findByStatusAndBookingDateBefore(eq("Pending"), any(LocalDateTime.class)))
                .thenReturn(List.of(matchBooking));

        int released = expiryService.releaseExpiredBookings();

        assertThat(released).isEqualTo(2);
        assertThat(cinemaBooking.getStatus()).isEqualTo("Cancelled");
        assertThat(cinemaSeat.getStatus()).isEqualTo("Available");
        assertThat(matchBooking.getStatus()).isEqualTo("Cancelled");
        assertThat(matchSeat.getStatus()).isEqualTo("Available");
    }
}

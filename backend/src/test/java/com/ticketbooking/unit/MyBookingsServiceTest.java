package com.ticketbooking.unit;

import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.ConcertSeat;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.MyBookingsService;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyBookingsServiceTest {

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
    @Mock
    private UserService userService;

    @InjectMocks
    private MyBookingsService myBookingsService;

    private ConcertBooking concertBookingOf(Long ownerId) {
        User owner = new User();
        owner.setUserId(ownerId);
        ConcertSeat seat = new ConcertSeat();
        seat.setStatus("Booked");
        ConcertBooking booking = new ConcertBooking();
        booking.setBookingId(7L);
        booking.setUser(owner);
        booking.setSeat(seat);
        booking.setStatus("Confirmed");
        return booking;
    }

    @Test
    void cancellingSomeoneElsesBookingThrows403() {
        User stranger = new User();
        stranger.setUserId(99L);
        ConcertBooking booking = concertBookingOf(1L);
        when(userService.getByEmail("stranger@mail.kz")).thenReturn(stranger);
        when(concertBookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> myBookingsService.cancelMyBooking("stranger@mail.kz", "concert", 7L));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(booking.getStatus()).isEqualTo("Confirmed");
        verify(concertBookingRepository, never()).save(any());
    }

    @Test
    void ownerCanCancelAndSeatBecomesAvailable() {
        User owner = new User();
        owner.setUserId(1L);
        ConcertBooking booking = concertBookingOf(1L);
        when(userService.getByEmail("amina@gmail.com")).thenReturn(owner);
        when(concertBookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        myBookingsService.cancelMyBooking("amina@gmail.com", "concert", 7L);

        assertThat(booking.getStatus()).isEqualTo("Cancelled");
        assertThat(booking.getSeat().getStatus()).isEqualTo("Available");
    }
}

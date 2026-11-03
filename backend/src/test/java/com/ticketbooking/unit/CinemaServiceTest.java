package com.ticketbooking.unit;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.Payment;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.CinemaService;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieSessionRepository sessionRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CinemaService cinemaService;

    private Seat createSeat(String status) {
        Seat seat = new Seat();
        seat.setSeatId(1L);
        seat.setSeatNumber("A1");
        seat.setPrice(new BigDecimal("2500"));
        seat.setStatus(status);
        return seat;
    }

    private User createUser() {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("Amina Seitkali");
        user.setEmail("amina@gmail.com");
        return user;
    }

    @Test
    void createBookingConfirmsBookingWhenSeatIsAvailable() {
        Seat seat = createSeat("Available");
        User user = createUser();
        BookingRequest request = new BookingRequest("Amina Seitkali", "amina@gmail.com", "+77011234567", 1L);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(userService.findOrCreate("Amina Seitkali", "amina@gmail.com", "+77011234567")).thenReturn(user);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = cinemaService.createBooking(request);

        assertEquals("Confirmed", booking.getStatus());
        assertEquals("Booked", seat.getStatus());
        assertEquals(user, booking.getUser());
    }

    @Test
    void createBookingSavesPaymentWithSeatPrice() {
        Seat seat = createSeat("Available");
        BookingRequest request = new BookingRequest("Amina Seitkali", "amina@gmail.com", "+77011234567", 1L);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(userService.findOrCreate(any(), any(), any())).thenReturn(createUser());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cinemaService.createBooking(request);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment payment = captor.getValue();
        assertEquals(new BigDecimal("2500"), payment.getAmount());
        assertEquals("Paid", payment.getStatus());
    }

    @Test
    void createBookingThrowsConflictWhenSeatIsTaken() {
        Seat seat = createSeat("Booked");
        BookingRequest request = new BookingRequest("Amina Seitkali", "amina@gmail.com", "+77011234567", 1L);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cinemaService.createBooking(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(bookingRepository, never()).save(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createBookingThrowsNotFoundWhenSeatDoesNotExist() {
        BookingRequest request = new BookingRequest("Amina Seitkali", "amina@gmail.com", "+77011234567", 999L);

        when(seatRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cinemaService.createBooking(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void cancelBookingMakesSeatAvailableAgain() {
        Seat seat = createSeat("Booked");
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setSeat(seat);
        booking.setStatus("Confirmed");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = cinemaService.cancelBooking(1L);

        assertEquals("Cancelled", result.getStatus());
        assertEquals("Available", seat.getStatus());
        verify(seatRepository).save(seat);
    }
}

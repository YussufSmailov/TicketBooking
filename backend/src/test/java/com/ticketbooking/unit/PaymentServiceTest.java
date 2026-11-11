package com.ticketbooking.unit;

import com.ticketbooking.dto.CardRequest;
import com.ticketbooking.dto.PaymentRequest;
import com.ticketbooking.dto.PaymentResult;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.ConcertSeat;
import com.ticketbooking.entity.Payment;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.CardService;
import com.ticketbooking.service.PaymentService;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ConcertBookingRepository concertBookingRepository;
    @Mock
    private ConcertSeatRepository concertSeatRepository;
    @Mock
    private ConcertPaymentRepository concertPaymentRepository;
    @Mock
    private MatchBookingRepository matchBookingRepository;
    @Mock
    private MatchSeatRepository matchSeatRepository;
    @Mock
    private MatchPaymentRepository matchPaymentRepository;
    @Mock
    private CardService cardService;
    @Mock
    private UserService userService;

    @InjectMocks
    private PaymentService paymentService;

    private User owner;
    private Booking booking;
    private Seat seat;

    @BeforeEach
    void createPendingBooking() {
        owner = new User();
        owner.setUserId(1L);
        owner.setEmail("amina@gmail.com");

        seat = new Seat();
        seat.setSeatId(10L);
        seat.setPrice(new BigDecimal("2500"));
        seat.setStatus("Reserved");

        booking = new Booking();
        booking.setBookingId(100L);
        booking.setUser(owner);
        booking.setSeat(seat);
        booking.setStatus("Pending");
        booking.setBookingDate(LocalDateTime.now().minusMinutes(2));
    }

    private PaymentRequest newCardRequest(String number) {
        return new PaymentRequest(null, new CardRequest(number, "AMINA SEITKALI", 12, 2030, "123"), false);
    }

    @Test
    void luhnCheckAcceptsValidAndRejectsInvalidNumbers() {
        assertThat(CardService.isValidLuhn("4242424242424242")).isTrue();
        assertThat(CardService.isValidLuhn("4000000000020000")).isTrue();
        assertThat(CardService.isValidLuhn("4242424242424241")).isFalse();
        assertThat(CardService.isValidLuhn("12ab")).isFalse();
    }

    @Test
    void cardEndingWith0000FailsAndReleasesSeat() {
        PaymentRequest request = newCardRequest("4000 0000 0002 0000");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.getByEmail("amina@gmail.com")).thenReturn(owner);
        when(cardService.findLast4ForPayment(owner, request)).thenReturn("0000");

        PaymentResult result = paymentService.payCinemaBooking(100L, "amina@gmail.com", request);

        assertThat(result.paymentStatus()).isEqualTo("Failed");
        assertThat(booking.getStatus()).isEqualTo("Cancelled");
        assertThat(seat.getStatus()).isEqualTo("Available");
        ArgumentCaptor<Payment> savedPayment = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(savedPayment.capture());
        assertThat(savedPayment.getValue().getStatus()).isEqualTo("Failed");
    }

    @Test
    void successfulPaymentConfirmsBookingAndBooksSeat() {
        PaymentRequest request = newCardRequest("4242 4242 4242 4242");
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.getByEmail("amina@gmail.com")).thenReturn(owner);
        when(cardService.findLast4ForPayment(owner, request)).thenReturn("4242");

        PaymentResult result = paymentService.payCinemaBooking(100L, "amina@gmail.com", request);

        assertThat(result.paymentStatus()).isEqualTo("Paid");
        assertThat(result.bookingStatus()).isEqualTo("Confirmed");
        assertThat(seat.getStatus()).isEqualTo("Booked");
        assertThat(result.amount()).isEqualByComparingTo("2500");
    }

    @Test
    void payingSomeoneElsesBookingThrows403() {
        User stranger = new User();
        stranger.setUserId(2L);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.getByEmail("arman@mail.kz")).thenReturn(stranger);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> paymentService.payCinemaBooking(100L, "arman@mail.kz", newCardRequest("4242424242424242")));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payingExpiredBookingThrows409() {
        booking.setBookingDate(LocalDateTime.now().minusMinutes(11));
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(userService.getByEmail("amina@gmail.com")).thenReturn(owner);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> paymentService.payCinemaBooking(100L, "amina@gmail.com", newCardRequest("4242424242424242")));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void concertPaymentWithSavedCardConfirmsConcertBooking() {
        ConcertSeat concertSeat = new ConcertSeat();
        concertSeat.setPrice(new BigDecimal("12000"));
        concertSeat.setStatus("Reserved");
        ConcertBooking concertBooking = new ConcertBooking();
        concertBooking.setBookingId(5L);
        concertBooking.setUser(owner);
        concertBooking.setSeat(concertSeat);
        concertBooking.setStatus("Pending");
        concertBooking.setBookingDate(LocalDateTime.now());
        PaymentRequest request = new PaymentRequest(1L, null, false);
        when(concertBookingRepository.findById(5L)).thenReturn(Optional.of(concertBooking));
        when(userService.getByEmail("amina@gmail.com")).thenReturn(owner);
        when(cardService.findLast4ForPayment(owner, request)).thenReturn("4242");

        PaymentResult result = paymentService.payConcertBooking(5L, "amina@gmail.com", request);

        assertThat(result.ticketNumber()).isEqualTo("CON-000005");
        assertThat(concertBooking.getStatus()).isEqualTo("Confirmed");
        assertThat(concertSeat.getStatus()).isEqualTo("Booked");
        verify(concertPaymentRepository).save(any());
        verify(cardService, never()).saveCard(any(), any());
    }
}

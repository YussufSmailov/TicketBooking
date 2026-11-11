package com.ticketbooking.service;

import com.ticketbooking.dto.PaymentRequest;
import com.ticketbooking.dto.PaymentResult;
import com.ticketbooking.entity.*;
import com.ticketbooking.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final ConcertBookingRepository concertBookingRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final ConcertPaymentRepository concertPaymentRepository;
    private final MatchBookingRepository matchBookingRepository;
    private final MatchSeatRepository matchSeatRepository;
    private final MatchPaymentRepository matchPaymentRepository;
    private final CardService cardService;
    private final UserService userService;

    @Value("${app.booking.reservation-minutes:10}")
    private long reservationMinutes = 10;

    public PaymentService(BookingRepository bookingRepository,
                          SeatRepository seatRepository,
                          PaymentRepository paymentRepository,
                          ConcertBookingRepository concertBookingRepository,
                          ConcertSeatRepository concertSeatRepository,
                          ConcertPaymentRepository concertPaymentRepository,
                          MatchBookingRepository matchBookingRepository,
                          MatchSeatRepository matchSeatRepository,
                          MatchPaymentRepository matchPaymentRepository,
                          CardService cardService,
                          UserService userService) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.paymentRepository = paymentRepository;
        this.concertBookingRepository = concertBookingRepository;
        this.concertSeatRepository = concertSeatRepository;
        this.concertPaymentRepository = concertPaymentRepository;
        this.matchBookingRepository = matchBookingRepository;
        this.matchSeatRepository = matchSeatRepository;
        this.matchPaymentRepository = matchPaymentRepository;
        this.cardService = cardService;
        this.userService = userService;
    }

    public static boolean isApproved(String last4) {
        return !"0000".equals(last4);
    }

    @Transactional
    public PaymentResult payCinemaBooking(Long bookingId, String userEmail, PaymentRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        User user = userService.getByEmail(userEmail);
        checkCanPay(booking.getUser(), user, booking.getStatus(), booking.getBookingDate());

        String last4 = cardService.findLast4ForPayment(user, request);
        boolean approved = isApproved(last4);
        Seat seat = booking.getSeat();

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(seat.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(approved ? "Paid" : "Failed");
        paymentRepository.save(payment);

        if (approved) {
            booking.setStatus("Confirmed");
            seat.setStatus("Booked");
            saveCardIfAsked(user, request);
        } else {
            booking.setStatus("Cancelled");
            seat.setStatus("Available");
        }
        seatRepository.save(seat);
        bookingRepository.save(booking);

        return buildResult("cinema", bookingId, seat.getPrice(), payment.getStatus(), booking.getStatus());
    }

    @Transactional
    public PaymentResult payConcertBooking(Long bookingId, String userEmail, PaymentRequest request) {
        ConcertBooking booking = concertBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        User user = userService.getByEmail(userEmail);
        checkCanPay(booking.getUser(), user, booking.getStatus(), booking.getBookingDate());

        String last4 = cardService.findLast4ForPayment(user, request);
        boolean approved = isApproved(last4);
        ConcertSeat seat = booking.getSeat();

        ConcertPayment payment = new ConcertPayment();
        payment.setBooking(booking);
        payment.setAmount(seat.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(approved ? "Paid" : "Failed");
        concertPaymentRepository.save(payment);

        if (approved) {
            booking.setStatus("Confirmed");
            seat.setStatus("Booked");
            saveCardIfAsked(user, request);
        } else {
            booking.setStatus("Cancelled");
            seat.setStatus("Available");
        }
        concertSeatRepository.save(seat);
        concertBookingRepository.save(booking);

        return buildResult("concert", bookingId, seat.getPrice(), payment.getStatus(), booking.getStatus());
    }

    @Transactional
    public PaymentResult payMatchBooking(Long bookingId, String userEmail, PaymentRequest request) {
        MatchBooking booking = matchBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
        User user = userService.getByEmail(userEmail);
        checkCanPay(booking.getUser(), user, booking.getStatus(), booking.getBookingDate());

        String last4 = cardService.findLast4ForPayment(user, request);
        boolean approved = isApproved(last4);
        MatchSeat seat = booking.getSeat();

        MatchPayment payment = new MatchPayment();
        payment.setBooking(booking);
        payment.setAmount(seat.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(approved ? "Paid" : "Failed");
        matchPaymentRepository.save(payment);

        if (approved) {
            booking.setStatus("Confirmed");
            seat.setStatus("Booked");
            saveCardIfAsked(user, request);
        } else {
            booking.setStatus("Cancelled");
            seat.setStatus("Available");
        }
        matchSeatRepository.save(seat);
        matchBookingRepository.save(booking);

        return buildResult("sport", bookingId, seat.getPrice(), payment.getStatus(), booking.getStatus());
    }

    private void checkCanPay(User owner, User currentUser, String status, LocalDateTime bookingDate) {
        if (!owner.getUserId().equals(currentUser.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Это не ваша бронь");
        }
        if (!status.equals("Pending")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Эта бронь уже оплачена или отменена");
        }
        if (bookingDate.isBefore(LocalDateTime.now().minusMinutes(reservationMinutes))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Время на оплату истекло, выберите место заново");
        }
    }

    private void saveCardIfAsked(User user, PaymentRequest request) {
        if (request.saveCard() && request.cardId() == null && request.newCard() != null) {
            cardService.saveCard(user, request.newCard());
        }
    }

    private PaymentResult buildResult(String type, Long bookingId, BigDecimal amount,
                                      String paymentStatus, String bookingStatus) {
        String message = paymentStatus.equals("Paid")
                ? "Оплата прошла. Билет в личном кабинете."
                : "Банк отклонил платёж. Бронь отменена, место снова свободно.";
        String ticketNumber = paymentStatus.equals("Paid") ? TicketNumber.create(type, bookingId) : null;
        return new PaymentResult(type, bookingId, amount, paymentStatus, bookingStatus, ticketNumber, message);
    }
}

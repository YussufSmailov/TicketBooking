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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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

    @Value("${app.booking.max-seats:6}")
    private int maxSeats = 6;

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

    public PaymentResult payCinemaBooking(Long bookingId, String userEmail, PaymentRequest request) {
        return payCinemaBookings(List.of(bookingId), userEmail, request);
    }

    public PaymentResult payConcertBooking(Long bookingId, String userEmail, PaymentRequest request) {
        return payConcertBookings(List.of(bookingId), userEmail, request);
    }

    public PaymentResult payMatchBooking(Long bookingId, String userEmail, PaymentRequest request) {
        return payMatchBookings(List.of(bookingId), userEmail, request);
    }

    /**
     * Оплата нескольких броней одной картой: либо все оплачены, либо (если банк отклонил) все отменены.
     * В таблице payments у каждой брони своя строка с ценой её места.
     */
    @Transactional
    public PaymentResult payCinemaBookings(List<Long> bookingIds, String userEmail, PaymentRequest request) {
        List<Long> ids = checkBookingIds(bookingIds);
        User user = userService.getByEmail(userEmail);
        List<Booking> bookings = new ArrayList<>();
        for (Long id : ids) {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
            checkCanPay(booking.getUser(), user, booking.getStatus(), booking.getBookingDate());
            bookings.add(booking);
        }

        boolean approved = isApproved(cardService.findLast4ForPayment(user, request));
        String paymentStatus = approved ? "Paid" : "Failed";
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : bookings) {
            Seat seat = booking.getSeat();
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(seat.getPrice());
            payment.setPaymentDate(now);
            payment.setStatus(paymentStatus);
            paymentRepository.save(payment);
            total = total.add(seat.getPrice());

            booking.setStatus(approved ? "Confirmed" : "Cancelled");
            seat.setStatus(approved ? "Booked" : "Available");
            seatRepository.save(seat);
            bookingRepository.save(booking);
        }

        if (approved) {
            saveCardIfAsked(user, request);
        }
        return buildResult("cinema", ids, total, paymentStatus);
    }

    @Transactional
    public PaymentResult payConcertBookings(List<Long> bookingIds, String userEmail, PaymentRequest request) {
        List<Long> ids = checkBookingIds(bookingIds);
        User user = userService.getByEmail(userEmail);
        List<ConcertBooking> bookings = new ArrayList<>();
        for (Long id : ids) {
            ConcertBooking booking = concertBookingRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
            checkCanPay(booking.getUser(), user, booking.getStatus(), booking.getBookingDate());
            bookings.add(booking);
        }

        boolean approved = isApproved(cardService.findLast4ForPayment(user, request));
        String paymentStatus = approved ? "Paid" : "Failed";
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (ConcertBooking booking : bookings) {
            ConcertSeat seat = booking.getSeat();
            ConcertPayment payment = new ConcertPayment();
            payment.setBooking(booking);
            payment.setAmount(seat.getPrice());
            payment.setPaymentDate(now);
            payment.setStatus(paymentStatus);
            concertPaymentRepository.save(payment);
            total = total.add(seat.getPrice());

            booking.setStatus(approved ? "Confirmed" : "Cancelled");
            seat.setStatus(approved ? "Booked" : "Available");
            concertSeatRepository.save(seat);
            concertBookingRepository.save(booking);
        }

        if (approved) {
            saveCardIfAsked(user, request);
        }
        return buildResult("concert", ids, total, paymentStatus);
    }

    @Transactional
    public PaymentResult payMatchBookings(List<Long> bookingIds, String userEmail, PaymentRequest request) {
        List<Long> ids = checkBookingIds(bookingIds);
        User user = userService.getByEmail(userEmail);
        List<MatchBooking> bookings = new ArrayList<>();
        for (Long id : ids) {
            MatchBooking booking = matchBookingRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
            checkCanPay(booking.getUser(), user, booking.getStatus(), booking.getBookingDate());
            bookings.add(booking);
        }

        boolean approved = isApproved(cardService.findLast4ForPayment(user, request));
        String paymentStatus = approved ? "Paid" : "Failed";
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (MatchBooking booking : bookings) {
            MatchSeat seat = booking.getSeat();
            MatchPayment payment = new MatchPayment();
            payment.setBooking(booking);
            payment.setAmount(seat.getPrice());
            payment.setPaymentDate(now);
            payment.setStatus(paymentStatus);
            matchPaymentRepository.save(payment);
            total = total.add(seat.getPrice());

            booking.setStatus(approved ? "Confirmed" : "Cancelled");
            seat.setStatus(approved ? "Booked" : "Available");
            matchSeatRepository.save(seat);
            matchBookingRepository.save(booking);
        }

        if (approved) {
            saveCardIfAsked(user, request);
        }
        return buildResult("sport", ids, total, paymentStatus);
    }

    private List<Long> checkBookingIds(List<Long> bookingIds) {
        if (bookingIds == null || bookingIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не выбраны брони для оплаты");
        }
        List<Long> ids = new ArrayList<>(new LinkedHashSet<>(bookingIds));
        if (ids.size() > maxSeats) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "За один раз можно оплатить не больше " + maxSeats + " броней");
        }
        if (ids.contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный номер брони");
        }
        return ids;
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

    private PaymentResult buildResult(String type, List<Long> bookingIds, BigDecimal total, String paymentStatus) {
        boolean paid = paymentStatus.equals("Paid");
        String message;
        if (paid) {
            message = bookingIds.size() == 1
                    ? "Оплата прошла. Билет в личном кабинете."
                    : "Оплата прошла. Билеты (" + bookingIds.size() + ") в личном кабинете.";
        } else {
            message = bookingIds.size() == 1
                    ? "Банк отклонил платёж. Бронь отменена, место снова свободно."
                    : "Банк отклонил платёж. Брони отменены, места снова свободны.";
        }
        List<String> ticketNumbers = new ArrayList<>();
        if (paid) {
            for (Long id : bookingIds) {
                ticketNumbers.add(TicketNumber.create(type, id));
            }
        }
        return new PaymentResult(type, bookingIds.get(0), total, paymentStatus, paid ? "Confirmed" : "Cancelled",
                paid ? ticketNumbers.get(0) : null, message, bookingIds, ticketNumbers);
    }
}

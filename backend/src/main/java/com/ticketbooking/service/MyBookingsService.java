package com.ticketbooking.service;

import com.ticketbooking.dto.MyTicket;
import com.ticketbooking.entity.*;
import com.ticketbooking.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MyBookingsService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final ConcertBookingRepository concertBookingRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final MatchBookingRepository matchBookingRepository;
    private final MatchSeatRepository matchSeatRepository;
    private final UserService userService;

    public MyBookingsService(BookingRepository bookingRepository,
                             SeatRepository seatRepository,
                             ConcertBookingRepository concertBookingRepository,
                             ConcertSeatRepository concertSeatRepository,
                             MatchBookingRepository matchBookingRepository,
                             MatchSeatRepository matchSeatRepository,
                             UserService userService) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.concertBookingRepository = concertBookingRepository;
        this.concertSeatRepository = concertSeatRepository;
        this.matchBookingRepository = matchBookingRepository;
        this.matchSeatRepository = matchSeatRepository;
        this.userService = userService;
    }

    public List<MyTicket> getMyBookings(String userEmail) {
        User user = userService.getByEmail(userEmail);
        List<MyTicket> tickets = new ArrayList<>();

        for (Booking booking : bookingRepository.findByUserUserIdOrderByBookingDateDesc(user.getUserId())) {
            Seat seat = booking.getSeat();
            MovieSession session = seat.getSession();
            tickets.add(new MyTicket("cinema", booking.getBookingId(), session.getSessionId(),
                    session.getMovie().getTitle(), session.getSessionDate(), session.getSessionTime(),
                    "Зал " + session.getHallNumber(), seat.getSeatNumber(), null, seat.getPrice(),
                    booking.getStatus(), booking.getBookingDate(), ticketNumber("cinema", booking.getBookingId(), booking.getStatus())));
        }

        for (ConcertBooking booking : concertBookingRepository.findByUserUserIdOrderByBookingDateDesc(user.getUserId())) {
            ConcertSeat seat = booking.getSeat();
            Concert concert = seat.getConcert();
            tickets.add(new MyTicket("concert", booking.getBookingId(), concert.getConcertId(),
                    concert.getArtist() + " — " + concert.getTitle(), concert.getConcertDate(), concert.getConcertTime(),
                    concert.getVenue() + ", " + concert.getCity(), seat.getSeatNumber(), seat.getZone(), seat.getPrice(),
                    booking.getStatus(), booking.getBookingDate(), ticketNumber("concert", booking.getBookingId(), booking.getStatus())));
        }

        for (MatchBooking booking : matchBookingRepository.findByUserUserIdOrderByBookingDateDesc(user.getUserId())) {
            MatchSeat seat = booking.getSeat();
            FootballMatch match = seat.getMatch();
            tickets.add(new MyTicket("sport", booking.getBookingId(), match.getMatchId(),
                    match.getHomeTeam().getName() + " — " + match.getAwayTeam().getName(), match.getMatchDate(), match.getMatchTime(),
                    match.getHomeTeam().getStadium(), seat.getSeatNumber(), seat.getSector(), seat.getPrice(),
                    booking.getStatus(), booking.getBookingDate(), ticketNumber("sport", booking.getBookingId(), booking.getStatus())));
        }

        tickets.sort(Comparator.comparing(MyTicket::bookingDate).reversed());
        return tickets;
    }

    private String ticketNumber(String type, Long bookingId, String status) {
        if (!status.equals("Confirmed")) {
            return null;
        }
        return TicketNumber.create(type, bookingId);
    }

    @Transactional
    public void cancelMyBooking(String userEmail, String type, Long bookingId) {
        User user = userService.getByEmail(userEmail);

        if (type.equals("cinema")) {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
            checkCanCancel(booking.getUser(), user, booking.getStatus());
            booking.setStatus("Cancelled");
            booking.getSeat().setStatus("Available");
            seatRepository.save(booking.getSeat());
            bookingRepository.save(booking);
        } else if (type.equals("concert")) {
            ConcertBooking booking = concertBookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
            checkCanCancel(booking.getUser(), user, booking.getStatus());
            booking.setStatus("Cancelled");
            booking.getSeat().setStatus("Available");
            concertSeatRepository.save(booking.getSeat());
            concertBookingRepository.save(booking);
        } else if (type.equals("sport")) {
            MatchBooking booking = matchBookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));
            checkCanCancel(booking.getUser(), user, booking.getStatus());
            booking.setStatus("Cancelled");
            booking.getSeat().setStatus("Available");
            matchSeatRepository.save(booking.getSeat());
            matchBookingRepository.save(booking);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Неизвестный тип брони");
        }
    }

    private void checkCanCancel(User owner, User currentUser, String status) {
        if (!owner.getUserId().equals(currentUser.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя отменить чужую бронь");
        }
        if (status.equals("Cancelled")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Бронь уже отменена");
        }
    }
}

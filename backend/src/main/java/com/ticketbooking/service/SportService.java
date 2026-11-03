package com.ticketbooking.service;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.SeatGroupRequest;
import com.ticketbooking.entity.FootballMatch;
import com.ticketbooking.entity.MatchBooking;
import com.ticketbooking.entity.MatchSeat;
import com.ticketbooking.entity.Team;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.FootballMatchRepository;
import com.ticketbooking.repository.MatchBookingRepository;
import com.ticketbooking.repository.MatchSeatRepository;
import com.ticketbooking.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SportService {

    private final TeamRepository teamRepository;
    private final FootballMatchRepository matchRepository;
    private final MatchSeatRepository seatRepository;
    private final MatchBookingRepository bookingRepository;
    private final UserService userService;

    public SportService(TeamRepository teamRepository,
                        FootballMatchRepository matchRepository,
                        MatchSeatRepository seatRepository,
                        MatchBookingRepository bookingRepository,
                        UserService userService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Команда не найдена"));
    }

    public List<FootballMatch> getAllMatches() {
        return matchRepository.findAllByOrderByMatchDateAscMatchTimeAsc();
    }

    public FootballMatch getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Матч не найден"));
    }

    public FootballMatch createMatch(FootballMatch match) {
        match.setHomeTeam(getTeamById(match.getHomeTeam().getTeamId()));
        match.setAwayTeam(getTeamById(match.getAwayTeam().getTeamId()));
        return matchRepository.save(match);
    }

    public FootballMatch updateMatch(Long id, FootballMatch newData) {
        FootballMatch match = getMatchById(id);
        match.setHomeTeam(getTeamById(newData.getHomeTeam().getTeamId()));
        match.setAwayTeam(getTeamById(newData.getAwayTeam().getTeamId()));
        match.setTournament(newData.getTournament());
        match.setRoundName(newData.getRoundName());
        match.setMatchDate(newData.getMatchDate());
        match.setMatchTime(newData.getMatchTime());
        return matchRepository.save(match);
    }

    @Transactional
    public void deleteMatch(Long id) {
        FootballMatch match = getMatchById(id);
        List<MatchSeat> seats = seatRepository.findByMatchMatchIdOrderBySeatIdAsc(id);
        seatRepository.deleteAll(seats);
        seatRepository.flush();
        matchRepository.delete(match);
    }

    @Transactional
    public List<MatchSeat> addSeats(Long matchId, SeatGroupRequest request) {
        request.validate();
        FootballMatch match = getMatchById(matchId);
        String sector = request.name().trim();
        String prefix = sector.substring(0, 1).toUpperCase();
        int existingSeats = seatRepository.findByMatchMatchIdOrderBySeatIdAsc(matchId).size();

        List<MatchSeat> newSeats = new ArrayList<>();
        for (int i = 1; i <= request.count(); i++) {
            MatchSeat seat = new MatchSeat();
            seat.setMatch(match);
            seat.setSector(sector);
            seat.setSeatNumber(prefix + "-" + (existingSeats + i));
            seat.setPrice(request.price());
            seat.setStatus("Available");
            newSeats.add(seat);
        }
        return seatRepository.saveAllAndFlush(newSeats);
    }

    public List<MatchSeat> getSeatsByMatch(Long matchId) {
        return seatRepository.findByMatchMatchIdOrderBySeatIdAsc(matchId);
    }

    public List<MatchBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public MatchBooking createBooking(BookingRequest request) {
        MatchSeat seat = seatRepository.findById(request.seatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено"));

        if (!seat.getStatus().equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это место уже занято");
        }

        User user = userService.findOrCreate(request.fullName(), request.email(), request.phone());

        MatchBooking booking = new MatchBooking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Confirmed");

        seat.setStatus("Booked");
        seatRepository.save(seat);

        return bookingRepository.save(booking);
    }

    @Transactional
    public MatchBooking cancelBooking(Long bookingId) {
        MatchBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        booking.setStatus("Cancelled");

        MatchSeat seat = booking.getSeat();
        seat.setStatus("Available");
        seatRepository.save(seat);

        return bookingRepository.save(booking);
    }
}

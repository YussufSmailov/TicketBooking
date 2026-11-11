package com.ticketbooking.service;

import com.ticketbooking.dto.MatchRequest;
import com.ticketbooking.dto.ZoneSeatRowRequest;
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

    public FootballMatch createMatch(MatchRequest request) {
        FootballMatch match = new FootballMatch();
        copyMatchFields(request, match);
        return matchRepository.save(match);
    }

    public FootballMatch updateMatch(Long id, MatchRequest request) {
        FootballMatch match = getMatchById(id);
        copyMatchFields(request, match);
        return matchRepository.save(match);
    }

    private void copyMatchFields(MatchRequest request, FootballMatch match) {
        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Команда не может играть сама с собой");
        }
        match.setHomeTeam(getTeamById(request.homeTeamId()));
        match.setAwayTeam(getTeamById(request.awayTeamId()));
        match.setTournament(request.tournament().trim());
        match.setRoundName(request.roundName());
        match.setMatchDate(request.matchDate());
        match.setMatchTime(request.matchTime());
        match.setDescription(request.description());
        match.setPosterUrl(request.posterUrl() == null || request.posterUrl().isBlank() ? null : request.posterUrl().trim());
    }

    public void deleteMatch(Long id) {
        FootballMatch match = getMatchById(id);
        matchRepository.delete(match);
    }

    public List<MatchSeat> getSeatsByMatch(Long matchId) {
        return seatRepository.findByMatchMatchIdOrderBySeatIdAsc(matchId);
    }

    @Transactional
    public List<MatchSeat> addSeatRow(Long matchId, ZoneSeatRowRequest request) {
        FootballMatch match = getMatchById(matchId);
        String sector = request.zone().trim();
        String firstSeatNumber = request.rowNumber() + "-1";
        if (seatRepository.existsByMatchMatchIdAndSectorAndSeatNumber(matchId, sector, firstSeatNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ряд " + request.rowNumber() + " уже есть в секторе " + sector);
        }

        List<MatchSeat> newSeats = new ArrayList<>();
        for (int number = 1; number <= request.seatCount(); number++) {
            MatchSeat seat = new MatchSeat();
            seat.setMatch(match);
            seat.setSector(sector);
            seat.setSeatNumber(request.rowNumber() + "-" + number);
            seat.setPrice(request.price());
            seat.setStatus("Available");
            newSeats.add(seat);
        }
        return seatRepository.saveAll(newSeats);
    }

    public List<MatchBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public MatchBooking createBooking(Long seatId, String userEmail) {
        MatchSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Место не найдено"));

        if (!seat.getStatus().equals("Available")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это место уже занято");
        }

        User user = userService.getByEmail(userEmail);

        MatchBooking booking = new MatchBooking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("Pending");

        seat.setStatus("Reserved");
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

package com.ticketbooking.service;

import com.ticketbooking.dto.MatchFilter;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SportService {

    private final TeamRepository teamRepository;
    private final FootballMatchRepository matchRepository;
    private final MatchSeatRepository seatRepository;
    private final MatchBookingRepository bookingRepository;
    private final UserService userService;
    private final ReviewService reviewService;

    @Value("${app.booking.max-seats:6}")
    private int maxSeats = 6;

    public SportService(TeamRepository teamRepository,
                        FootballMatchRepository matchRepository,
                        MatchSeatRepository seatRepository,
                        MatchBookingRepository bookingRepository,
                        UserService userService,
                        ReviewService reviewService) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.reviewService = reviewService;
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

    public List<FootballMatch> searchMatches(MatchFilter filter) {
        List<FootballMatch> result = new ArrayList<>();
        for (FootballMatch match : matchRepository.findAllByOrderByMatchDateAscMatchTimeAsc()) {
            Team home = match.getHomeTeam();
            Team away = match.getAwayTeam();
            if (!TextSearch.matches(filter.q(), home.getName(), away.getName(), home.getCity(), away.getCity(),
                    home.getStadium(), match.getTournament(), match.getRoundName(), match.getDescription())) continue;
            if (filter.teamId() != null && !filter.teamId().equals(home.getTeamId()) && !filter.teamId().equals(away.getTeamId())) continue;
            if (filter.minRating() != null && (match.getRating() == null || match.getRating() < filter.minRating())) continue;
            if (filter.dateFrom() != null && match.getMatchDate().isBefore(filter.dateFrom())) continue;
            if (filter.dateTo() != null && match.getMatchDate().isAfter(filter.dateTo())) continue;
            result.add(match);
        }
        result.sort(matchOrder(filter.sort()));
        return result;
    }

    private Comparator<FootballMatch> matchOrder(String sort) {
        Comparator<FootballMatch> byDate = Comparator.comparing(FootballMatch::getMatchDate).thenComparing(FootballMatch::getMatchTime);
        if (sort == null || sort.isBlank() || sort.equals("date")) {
            return byDate;
        }
        if (sort.equals("rating")) {
            return Comparator.comparing(FootballMatch::getRating, Comparator.nullsLast(Comparator.reverseOrder())).thenComparing(byDate);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестная сортировка: " + sort);
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

    @Transactional
    public void deleteMatch(Long id) {
        FootballMatch match = getMatchById(id);
        reviewService.deleteReviewsOf("match", id);
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
        return createBookings(List.of(seatId), userEmail).get(0);
    }

    /**
     * Бронирует сразу несколько мест одного матча. Если хоть одно место занято, не бронируется ни одно.
     */
    @Transactional
    public List<MatchBooking> createBookings(List<Long> seatIds, String userEmail) {
        SeatSelection.check(seatIds, maxSeats);
        List<MatchSeat> seats = seatRepository.findAllById(seatIds);
        SeatSelection.checkAllFound(seatIds.size(), seats.size());

        Set<Long> events = new HashSet<>();
        for (MatchSeat seat : seats) {
            events.add(seat.getMatch().getMatchId());
            SeatSelection.checkAvailable(seat.getStatus());
        }
        SeatSelection.checkSameEvent(events);

        User user = userService.getByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();
        List<MatchBooking> bookings = new ArrayList<>();
        for (MatchSeat seat : seats) {
            MatchBooking booking = new MatchBooking();
            booking.setUser(user);
            booking.setSeat(seat);
            booking.setBookingDate(now);
            booking.setStatus("Pending");
            seat.setStatus("Reserved");
            bookings.add(booking);
        }
        seatRepository.saveAll(seats);
        return bookingRepository.saveAll(bookings);
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

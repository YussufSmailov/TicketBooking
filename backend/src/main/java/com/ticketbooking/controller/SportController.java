package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.MatchFilter;
import com.ticketbooking.dto.MatchRequest;
import com.ticketbooking.dto.ZoneSeatRowRequest;
import com.ticketbooking.entity.FootballMatch;
import com.ticketbooking.entity.MatchBooking;
import com.ticketbooking.entity.MatchSeat;
import com.ticketbooking.entity.Team;
import com.ticketbooking.service.SportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/sport")
public class SportController {

    private final SportService sportService;

    public SportController(SportService sportService) {
        this.sportService = sportService;
    }

    @GetMapping("/teams")
    public List<Team> getAllTeams() {
        return sportService.getAllTeams();
    }

    @GetMapping("/matches")
    public List<FootballMatch> getMatches(@ModelAttribute MatchFilter filter) {
        return sportService.searchMatches(filter);
    }

    @GetMapping("/matches/{id}")
    public FootballMatch getMatch(@PathVariable Long id) {
        return sportService.getMatchById(id);
    }

    @PostMapping("/matches")
    @ResponseStatus(HttpStatus.CREATED)
    public FootballMatch createMatch(@Valid @RequestBody MatchRequest request) {
        return sportService.createMatch(request);
    }

    @PutMapping("/matches/{id}")
    public FootballMatch updateMatch(@PathVariable Long id, @Valid @RequestBody MatchRequest request) {
        return sportService.updateMatch(id, request);
    }

    @DeleteMapping("/matches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatch(@PathVariable Long id) {
        sportService.deleteMatch(id);
    }

    @GetMapping("/matches/{id}/seats")
    public List<MatchSeat> getSeats(@PathVariable Long id) {
        return sportService.getSeatsByMatch(id);
    }

    @PostMapping("/matches/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchSeat> addSeatRow(@PathVariable Long id, @Valid @RequestBody ZoneSeatRowRequest request) {
        return sportService.addSeatRow(id, request);
    }

    @GetMapping("/bookings")
    public List<MatchBooking> getAllBookings() {
        return sportService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchBooking> createBooking(@RequestBody BookingRequest request, Principal principal) {
        return sportService.createBookings(request.allSeatIds(), principal.getName());
    }

    @PutMapping("/bookings/{id}/cancel")
    public MatchBooking cancelBooking(@PathVariable Long id) {
        return sportService.cancelBooking(id);
    }
}

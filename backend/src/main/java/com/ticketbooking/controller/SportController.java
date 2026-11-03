package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.SeatGroupRequest;
import com.ticketbooking.entity.FootballMatch;
import com.ticketbooking.entity.MatchBooking;
import com.ticketbooking.entity.MatchSeat;
import com.ticketbooking.entity.Team;
import com.ticketbooking.service.SportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sport")
@CrossOrigin(origins = "*")
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
    public List<FootballMatch> getAllMatches() {
        return sportService.getAllMatches();
    }

    @GetMapping("/matches/{id}")
    public FootballMatch getMatch(@PathVariable Long id) {
        return sportService.getMatchById(id);
    }

    @PostMapping("/matches")
    @ResponseStatus(HttpStatus.CREATED)
    public FootballMatch createMatch(@RequestBody FootballMatch match) {
        return sportService.createMatch(match);
    }

    @PutMapping("/matches/{id}")
    public FootballMatch updateMatch(@PathVariable Long id, @RequestBody FootballMatch match) {
        return sportService.updateMatch(id, match);
    }

    @DeleteMapping("/matches/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatch(@PathVariable Long id) {
        sportService.deleteMatch(id);
    }

    @PostMapping("/matches/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MatchSeat> addSeats(@PathVariable Long id, @RequestBody SeatGroupRequest request) {
        return sportService.addSeats(id, request);
    }

    @GetMapping("/matches/{id}/seats")
    public List<MatchSeat> getSeats(@PathVariable Long id) {
        return sportService.getSeatsByMatch(id);
    }

    @GetMapping("/bookings")
    public List<MatchBooking> getAllBookings() {
        return sportService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchBooking createBooking(@RequestBody BookingRequest request) {
        return sportService.createBooking(request);
    }

    @PutMapping("/bookings/{id}/cancel")
    public MatchBooking cancelBooking(@PathVariable Long id) {
        return sportService.cancelBooking(id);
    }
}

package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.ConcertFilter;
import com.ticketbooking.dto.ConcertRequest;
import com.ticketbooking.dto.ZoneSeatRowRequest;
import com.ticketbooking.entity.Concert;
import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.ConcertSeat;
import com.ticketbooking.service.ConcertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/concert")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping("/concerts")
    public List<Concert> getConcerts(@ModelAttribute ConcertFilter filter) {
        return concertService.searchConcerts(filter);
    }

    @GetMapping("/concerts/{id}")
    public Concert getConcert(@PathVariable Long id) {
        return concertService.getConcertById(id);
    }

    @PostMapping("/concerts")
    @ResponseStatus(HttpStatus.CREATED)
    public Concert createConcert(@Valid @RequestBody ConcertRequest request) {
        return concertService.createConcert(request);
    }

    @PutMapping("/concerts/{id}")
    public Concert updateConcert(@PathVariable Long id, @Valid @RequestBody ConcertRequest request) {
        return concertService.updateConcert(id, request);
    }

    @DeleteMapping("/concerts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConcert(@PathVariable Long id) {
        concertService.deleteConcert(id);
    }

    @GetMapping("/concerts/{id}/seats")
    public List<ConcertSeat> getSeats(@PathVariable Long id) {
        return concertService.getSeatsByConcert(id);
    }

    @PostMapping("/concerts/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ConcertSeat> addSeatRow(@PathVariable Long id, @Valid @RequestBody ZoneSeatRowRequest request) {
        return concertService.addSeatRow(id, request);
    }

    @GetMapping("/bookings")
    public List<ConcertBooking> getAllBookings() {
        return concertService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ConcertBooking> createBooking(@RequestBody BookingRequest request, Principal principal) {
        return concertService.createBookings(request.allSeatIds(), principal.getName());
    }

    @PutMapping("/bookings/{id}/cancel")
    public ConcertBooking cancelBooking(@PathVariable Long id) {
        return concertService.cancelBooking(id);
    }
}

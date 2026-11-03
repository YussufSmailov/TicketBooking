package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.SeatGroupRequest;
import com.ticketbooking.entity.Concert;
import com.ticketbooking.entity.ConcertBooking;
import com.ticketbooking.entity.ConcertSeat;
import com.ticketbooking.service.ConcertService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concert")
@CrossOrigin(origins = "*")
public class ConcertController {

    private final ConcertService concertService;

    public ConcertController(ConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping("/concerts")
    public List<Concert> getAllConcerts() {
        return concertService.getAllConcerts();
    }

    @GetMapping("/concerts/{id}")
    public Concert getConcert(@PathVariable Long id) {
        return concertService.getConcertById(id);
    }

    @PostMapping("/concerts")
    @ResponseStatus(HttpStatus.CREATED)
    public Concert createConcert(@RequestBody Concert concert) {
        return concertService.createConcert(concert);
    }

    @PutMapping("/concerts/{id}")
    public Concert updateConcert(@PathVariable Long id, @RequestBody Concert concert) {
        return concertService.updateConcert(id, concert);
    }

    @DeleteMapping("/concerts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConcert(@PathVariable Long id) {
        concertService.deleteConcert(id);
    }

    @PostMapping("/concerts/{id}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ConcertSeat> addSeats(@PathVariable Long id, @RequestBody SeatGroupRequest request) {
        return concertService.addSeats(id, request);
    }

    @GetMapping("/concerts/{id}/seats")
    public List<ConcertSeat> getSeats(@PathVariable Long id) {
        return concertService.getSeatsByConcert(id);
    }

    @GetMapping("/bookings")
    public List<ConcertBooking> getAllBookings() {
        return concertService.getAllBookings();
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public ConcertBooking createBooking(@RequestBody BookingRequest request) {
        return concertService.createBooking(request);
    }

    @PutMapping("/bookings/{id}/cancel")
    public ConcertBooking cancelBooking(@PathVariable Long id) {
        return concertService.cancelBooking(id);
    }
}

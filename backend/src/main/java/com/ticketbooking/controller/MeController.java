package com.ticketbooking.controller;

import com.ticketbooking.dto.CardRequest;
import com.ticketbooking.dto.CardResponse;
import com.ticketbooking.dto.MyTicket;
import com.ticketbooking.dto.ProfileRequest;
import com.ticketbooking.dto.UserResponse;
import com.ticketbooking.service.AuthService;
import com.ticketbooking.service.CardService;
import com.ticketbooking.service.MyBookingsService;
import com.ticketbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AuthService authService;
    private final CardService cardService;
    private final MyBookingsService myBookingsService;
    private final UserService userService;

    public MeController(AuthService authService,
                        CardService cardService,
                        MyBookingsService myBookingsService,
                        UserService userService) {
        this.authService = authService;
        this.cardService = cardService;
        this.myBookingsService = myBookingsService;
        this.userService = userService;
    }

    @GetMapping
    public UserResponse getMe(Principal principal) {
        return authService.getCurrentUser(principal.getName());
    }

    @PutMapping
    public UserResponse updateMe(@Valid @RequestBody ProfileRequest request, Principal principal) {
        userService.updateProfile(principal.getName(), request);
        return authService.getCurrentUser(principal.getName());
    }

    @GetMapping("/bookings")
    public List<MyTicket> getMyBookings(Principal principal) {
        return myBookingsService.getMyBookings(principal.getName());
    }

    @PutMapping("/bookings/{type}/{id}/cancel")
    public void cancelMyBooking(@PathVariable String type, @PathVariable Long id, Principal principal) {
        myBookingsService.cancelMyBooking(principal.getName(), type, id);
    }

    @GetMapping("/cards")
    public List<CardResponse> getMyCards(Principal principal) {
        return cardService.getCards(principal.getName());
    }

    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse addCard(@Valid @RequestBody CardRequest request, Principal principal) {
        return cardService.addCard(principal.getName(), request);
    }

    @DeleteMapping("/cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id, Principal principal) {
        cardService.deleteCard(principal.getName(), id);
    }
}

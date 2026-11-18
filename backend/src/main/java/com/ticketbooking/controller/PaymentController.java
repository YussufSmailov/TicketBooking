package com.ticketbooking.controller;

import com.ticketbooking.dto.PaymentRequest;
import com.ticketbooking.dto.PaymentResult;
import com.ticketbooking.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{type}")
    public PaymentResult payMany(@PathVariable String type,
                                 @Valid @RequestBody PaymentRequest request,
                                 Principal principal) {
        if (type.equals("cinema")) {
            return paymentService.payCinemaBookings(request.bookingIds(), principal.getName(), request);
        }
        if (type.equals("concert")) {
            return paymentService.payConcertBookings(request.bookingIds(), principal.getName(), request);
        }
        if (type.equals("sport")) {
            return paymentService.payMatchBookings(request.bookingIds(), principal.getName(), request);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Неизвестный тип брони");
    }

    @PostMapping("/{type}/{bookingId}")
    public PaymentResult pay(@PathVariable String type,
                             @PathVariable Long bookingId,
                             @Valid @RequestBody PaymentRequest request,
                             Principal principal) {
        if (type.equals("cinema")) {
            return paymentService.payCinemaBooking(bookingId, principal.getName(), request);
        }
        if (type.equals("concert")) {
            return paymentService.payConcertBooking(bookingId, principal.getName(), request);
        }
        if (type.equals("sport")) {
            return paymentService.payMatchBooking(bookingId, principal.getName(), request);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Неизвестный тип брони");
    }
}

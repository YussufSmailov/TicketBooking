package com.ticketbooking.controller;

import com.ticketbooking.dto.ReviewRequest;
import com.ticketbooking.dto.ReviewResponse;
import com.ticketbooking.dto.ReviewSummary;
import com.ticketbooking.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{eventType}/{eventId}")
    public ReviewSummary getReviews(@PathVariable String eventType, @PathVariable Long eventId, Principal principal) {
        return reviewService.getReviews(eventType, eventId, principal == null ? null : principal.getName());
    }

    @PostMapping("/{eventType}/{eventId}")
    public ReviewResponse saveReview(@PathVariable String eventType,
                                     @PathVariable Long eventId,
                                     @Valid @RequestBody ReviewRequest request,
                                     Principal principal) {
        return reviewService.saveReview(eventType, eventId, principal.getName(), request);
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Long reviewId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        reviewService.deleteReview(reviewId, authentication.getName(), isAdmin);
    }
}

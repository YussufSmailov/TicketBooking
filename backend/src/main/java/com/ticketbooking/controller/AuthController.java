package com.ticketbooking.controller;

import com.ticketbooking.dto.AuthResponse;
import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.dto.RegisterRequest;
import com.ticketbooking.dto.ResendCodeRequest;
import com.ticketbooking.dto.VerificationSentResponse;
import com.ticketbooking.dto.VerifyEmailRequest;
import com.ticketbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public VerificationSentResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify")
    public AuthResponse verify(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request.email(), request.code());
    }

    @PostMapping("/resend-code")
    public VerificationSentResponse resendCode(@Valid @RequestBody ResendCodeRequest request) {
        return authService.resendCode(request.email());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

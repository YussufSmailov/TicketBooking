package com.ticketbooking.service;

import com.ticketbooking.dto.AuthResponse;
import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.dto.RegisterRequest;
import com.ticketbooking.dto.UserResponse;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.UserCredentials;
import com.ticketbooking.repository.UserCredentialsRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       UserCredentialsRepository credentialsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user = userRepository.save(user);

        UserCredentials credentials = saveCredentials(user, request.password(), ROLE_CUSTOMER);
        return buildResponse(user, credentials.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        UserCredentials credentials = credentialsRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));

        if (!passwordEncoder.matches(request.password(), credentials.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }

        return buildResponse(credentials.getUser(), credentials.getRole());
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
        return toResponse(user, getRole(user));
    }

    public String getRole(User user) {
        return credentialsRepository.findById(user.getUserId())
                .map(UserCredentials::getRole)
                .orElse(ROLE_CUSTOMER);
    }

    public UserCredentials saveCredentials(User user, String password, String role) {
        UserCredentials credentials = new UserCredentials();
        credentials.setUser(user);
        credentials.setPasswordHash(passwordEncoder.encode(password));
        credentials.setRole(role);
        return credentialsRepository.save(credentials);
    }

    public UserResponse toResponse(User user, String role) {
        return new UserResponse(user.getUserId(), user.getFullName(), user.getEmail(), user.getPhone(), role);
    }

    private AuthResponse buildResponse(User user, String role) {
        String token = jwtService.createToken(user.getEmail(), role);
        return new AuthResponse(token, toResponse(user, role));
    }
}

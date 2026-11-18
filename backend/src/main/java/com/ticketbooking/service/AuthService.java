package com.ticketbooking.service;

import com.ticketbooking.dto.AuthResponse;
import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.dto.RegisterRequest;
import com.ticketbooking.dto.UserResponse;
import com.ticketbooking.dto.VerificationSentResponse;
import com.ticketbooking.entity.EmailVerification;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.UserCredentials;
import com.ticketbooking.exception.EmailNotVerifiedException;
import com.ticketbooking.repository.EmailVerificationRepository;
import com.ticketbooking.repository.UserCredentialsRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_ADMIN = "ADMIN";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.verification.code-minutes:15}")
    private long codeMinutes = 15;

    @Value("${app.verification.resend-seconds:60}")
    private long resendSeconds = 60;

    @Value("${app.verification.max-attempts:5}")
    private int maxAttempts = 5;

    public AuthService(UserRepository userRepository,
                       UserCredentialsRepository credentialsRepository,
                       EmailVerificationRepository verificationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Создаёт неподтверждённый аккаунт и отправляет код на email.
     * Если аккаунт с этим email уже есть, но ещё не подтверждён, данные обновляются и код отправляется заново:
     * так человек, потерявший письмо, может просто зарегистрироваться ещё раз.
     */
    @Transactional
    public VerificationSentResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        Optional<User> existing = userRepository.findByEmail(email);

        User user;
        UserCredentials credentials;
        if (existing.isPresent()) {
            user = existing.get();
            credentials = credentialsRepository.findById(user.getUserId()).orElse(null);
            if (credentials == null || credentials.isEmailVerified()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже зарегистрирован");
            }
            checkResendAllowed(user);
        } else {
            user = new User();
            credentials = null;
        }

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user = userRepository.save(user);

        if (credentials == null) {
            saveCredentials(user, request.password(), ROLE_CUSTOMER, false);
        } else {
            credentials.setPasswordHash(passwordEncoder.encode(request.password()));
            credentialsRepository.save(credentials);
        }

        sendNewCode(user);
        return sentResponse(email);
    }

    @Transactional
    public AuthResponse verifyEmail(String rawEmail, String code) {
        String email = normalize(rawEmail);
        UserCredentials credentials = credentialsRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт с таким email не найден"));
        if (credentials.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже подтверждён, просто войдите");
        }

        EmailVerification verification = verificationRepository.findById(credentials.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Код не найден, запросите новый"));

        if (verification.getAttempts() >= maxAttempts) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Слишком много неверных попыток, запросите новый код");
        }
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Срок действия кода истёк, запросите новый");
        }
        if (!passwordEncoder.matches(code, verification.getCodeHash())) {
            verification.setAttempts(verification.getAttempts() + 1);
            verificationRepository.save(verification);
            int left = maxAttempts - verification.getAttempts();
            String message = left > 0
                    ? "Неверный код. Осталось попыток: " + left
                    : "Слишком много неверных попыток, запросите новый код";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        credentials.setEmailVerified(true);
        credentialsRepository.save(credentials);
        verificationRepository.delete(verification);
        return buildResponse(credentials.getUser(), credentials.getRole());
    }

    @Transactional
    public VerificationSentResponse resendCode(String rawEmail) {
        String email = normalize(rawEmail);
        UserCredentials credentials = credentialsRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Аккаунт с таким email не найден"));
        if (credentials.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже подтверждён, просто войдите");
        }
        checkResendAllowed(credentials.getUser());
        sendNewCode(credentials.getUser());
        return sentResponse(email);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        UserCredentials credentials = credentialsRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль"));

        if (!passwordEncoder.matches(request.password(), credentials.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        }
        if (!credentials.isEmailVerified()) {
            throw new EmailNotVerifiedException(email);
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

    public UserCredentials saveCredentials(User user, String password, String role, boolean emailVerified) {
        UserCredentials credentials = new UserCredentials();
        credentials.setUser(user);
        credentials.setPasswordHash(passwordEncoder.encode(password));
        credentials.setRole(role);
        credentials.setEmailVerified(emailVerified);
        return credentialsRepository.save(credentials);
    }

    public UserResponse toResponse(User user, String role) {
        return new UserResponse(user.getUserId(), user.getFullName(), user.getEmail(), user.getPhone(), role);
    }

    public static String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private void checkResendAllowed(User user) {
        Optional<EmailVerification> previous = verificationRepository.findById(user.getUserId());
        if (previous.isEmpty()) {
            return;
        }
        LocalDateTime nextAllowed = previous.get().getSentAt().plusSeconds(resendSeconds);
        if (nextAllowed.isAfter(LocalDateTime.now())) {
            long wait = Math.max(1, Duration.between(LocalDateTime.now(), nextAllowed).toSeconds());
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Новый код можно запросить через " + wait + " сек.");
        }
    }

    private void sendNewCode(User user) {
        String code = generateCode();
        LocalDateTime now = LocalDateTime.now();

        EmailVerification verification = verificationRepository.findById(user.getUserId()).orElseGet(() -> {
            EmailVerification created = new EmailVerification();
            created.setUser(user);
            return created;
        });
        verification.setCodeHash(passwordEncoder.encode(code));
        verification.setSentAt(now);
        verification.setExpiresAt(now.plusMinutes(codeMinutes));
        verification.setAttempts(0);
        verificationRepository.save(verification);

        emailService.sendVerificationCode(user.getEmail(), user.getFullName(), code);
    }

    private VerificationSentResponse sentResponse(String email) {
        return new VerificationSentResponse(email, "Мы отправили 6-значный код на " + email,
                resendSeconds, codeMinutes);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private AuthResponse buildResponse(User user, String role) {
        String token = jwtService.createToken(user.getEmail(), role);
        return new AuthResponse(token, toResponse(user, role));
    }
}

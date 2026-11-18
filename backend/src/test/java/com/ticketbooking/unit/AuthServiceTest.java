package com.ticketbooking.unit;

import com.ticketbooking.dto.AuthResponse;
import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.dto.RegisterRequest;
import com.ticketbooking.dto.VerificationSentResponse;
import com.ticketbooking.entity.EmailVerification;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.UserCredentials;
import com.ticketbooking.exception.EmailNotVerifiedException;
import com.ticketbooking.repository.EmailVerificationRepository;
import com.ticketbooking.repository.UserCredentialsRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.service.AuthService;
import com.ticketbooking.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialsRepository credentialsRepository;

    @Mock
    private EmailVerificationRepository verificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private UserCredentials unverifiedCredentials() {
        User user = new User();
        user.setUserId(5L);
        user.setFullName("Arman Zhaksylyk");
        user.setEmail("arman@mail.kz");
        UserCredentials credentials = new UserCredentials();
        credentials.setUserId(5L);
        credentials.setUser(user);
        credentials.setPasswordHash("hashed-password");
        credentials.setRole("CUSTOMER");
        credentials.setEmailVerified(false);
        return credentials;
    }

    private EmailVerification verification(UserCredentials credentials, LocalDateTime expiresAt, int attempts) {
        EmailVerification verification = new EmailVerification();
        verification.setUserId(credentials.getUserId());
        verification.setUser(credentials.getUser());
        verification.setCodeHash("hashed-code");
        verification.setSentAt(LocalDateTime.now().minusMinutes(2));
        verification.setExpiresAt(expiresAt);
        verification.setAttempts(attempts);
        return verification;
    }

    @Test
    void registerSavesUnverifiedUserAndEmailsSixDigitCode() {
        RegisterRequest request = new RegisterRequest("Arman Zhaksylyk", "Arman@Mail.kz", "+77029876543", "Secret123");
        when(userRepository.findByEmail("arman@mail.kz")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(call -> {
            User user = call.getArgument(0);
            user.setUserId(5L);
            return user;
        });
        when(passwordEncoder.encode(anyString())).thenAnswer(call -> "hashed:" + call.getArgument(0));
        when(credentialsRepository.save(any(UserCredentials.class))).thenAnswer(call -> call.getArgument(0));
        when(verificationRepository.findById(5L)).thenReturn(Optional.empty());

        VerificationSentResponse response = authService.register(request);

        assertThat(response.email()).isEqualTo("arman@mail.kz");
        verify(credentialsRepository).save(org.mockito.ArgumentMatchers.argThat(
                credentials -> credentials.getPasswordHash().equals("hashed:Secret123") && !credentials.isEmailVerified()));

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationCode(eq("arman@mail.kz"), eq("Arman Zhaksylyk"), code.capture());
        assertThat(code.getValue()).matches("[0-9]{6}");
        verify(verificationRepository).save(org.mockito.ArgumentMatchers.argThat(
                saved -> saved.getCodeHash().equals("hashed:" + code.getValue())));
        verify(jwtService, never()).createToken(any(), any());
    }

    @Test
    void registerWithVerifiedEmailThrows409() {
        RegisterRequest request = new RegisterRequest("Arman Zhaksylyk", "arman@mail.kz", "+77029876543", "Secret123");
        UserCredentials credentials = unverifiedCredentials();
        credentials.setEmailVerified(true);
        when(userRepository.findByEmail("arman@mail.kz")).thenReturn(Optional.of(credentials.getUser()));
        when(credentialsRepository.findById(5L)).thenReturn(Optional.of(credentials));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendVerificationCode(any(), any(), any());
    }

    @Test
    void loginWithWrongPasswordThrows401() {
        UserCredentials credentials = unverifiedCredentials();
        credentials.setEmailVerified(true);
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("WrongPass1", "hashed-password")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("arman@mail.kz", "WrongPass1")));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtService, never()).createToken(any(), any());
    }

    @Test
    void loginBeforeEmailVerificationIsRejected() {
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(unverifiedCredentials()));
        when(passwordEncoder.matches("Secret123", "hashed-password")).thenReturn(true);

        assertThrows(EmailNotVerifiedException.class,
                () -> authService.login(new LoginRequest("arman@mail.kz", "Secret123")));
        verify(jwtService, never()).createToken(any(), any());
    }

    @Test
    void correctCodeVerifiesEmailAndReturnsToken() {
        UserCredentials credentials = unverifiedCredentials();
        EmailVerification verification = verification(credentials, LocalDateTime.now().plusMinutes(10), 0);
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(verification));
        when(passwordEncoder.matches("123456", "hashed-code")).thenReturn(true);
        when(jwtService.createToken("arman@mail.kz", "CUSTOMER")).thenReturn("token-123");

        AuthResponse response = authService.verifyEmail("Arman@mail.kz", "123456");

        assertThat(response.token()).isEqualTo("token-123");
        assertThat(credentials.isEmailVerified()).isTrue();
        verify(verificationRepository).delete(verification);
    }

    @Test
    void wrongCodeCountsAttempt() {
        UserCredentials credentials = unverifiedCredentials();
        EmailVerification verification = verification(credentials, LocalDateTime.now().plusMinutes(10), 1);
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(verification));
        when(passwordEncoder.matches("000000", "hashed-code")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmail("arman@mail.kz", "000000"));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(verification.getAttempts()).isEqualTo(2);
        assertThat(credentials.isEmailVerified()).isFalse();
    }

    @Test
    void expiredCodeIsRejectedEvenIfCorrect() {
        UserCredentials credentials = unverifiedCredentials();
        EmailVerification verification = verification(credentials, LocalDateTime.now().minusMinutes(1), 0);
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(verification));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmail("arman@mail.kz", "123456"));

        assertThat(error.getReason()).contains("истёк");
        assertThat(credentials.isEmailVerified()).isFalse();
    }

    @Test
    void tooManyAttemptsBlocksCode() {
        UserCredentials credentials = unverifiedCredentials();
        EmailVerification verification = verification(credentials, LocalDateTime.now().plusMinutes(10), 5);
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(verification));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.verifyEmail("arman@mail.kz", "123456"));

        assertThat(error.getReason()).contains("попыток");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void resendTooSoonReturns429() {
        UserCredentials credentials = unverifiedCredentials();
        EmailVerification verification = verification(credentials, LocalDateTime.now().plusMinutes(10), 0);
        verification.setSentAt(LocalDateTime.now().minusSeconds(10));
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(verification));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.resendCode("arman@mail.kz"));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verify(emailService, never()).sendVerificationCode(any(), any(), any());
    }

    @Test
    void generatedCodeIsAlwaysSixDigits() {
        for (int i = 0; i < 200; i++) {
            assertThat(AuthService.generateCode()).matches("[0-9]{6}");
        }
    }
}

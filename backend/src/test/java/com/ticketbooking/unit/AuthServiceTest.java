package com.ticketbooking.unit;

import com.ticketbooking.dto.AuthResponse;
import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.dto.RegisterRequest;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.UserCredentials;
import com.ticketbooking.repository.UserCredentialsRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSavesUserWithHashedPasswordAndCustomerRole() {
        RegisterRequest request = new RegisterRequest("Arman Zhaksylyk", "Arman@Mail.kz", "+77029876543", "Secret123");
        when(userRepository.findByEmail("arman@mail.kz")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(call -> {
            User user = call.getArgument(0);
            user.setUserId(5L);
            return user;
        });
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed-password");
        when(credentialsRepository.save(any(UserCredentials.class))).thenAnswer(call -> call.getArgument(0));
        when(jwtService.createToken("arman@mail.kz", "CUSTOMER")).thenReturn("token-123");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("token-123");
        assertThat(response.user().email()).isEqualTo("arman@mail.kz");
        assertThat(response.user().role()).isEqualTo("CUSTOMER");
        verify(credentialsRepository).save(org.mockito.ArgumentMatchers.argThat(
                credentials -> credentials.getPasswordHash().equals("hashed-password")));
    }

    @Test
    void registerWithExistingEmailThrows409() {
        RegisterRequest request = new RegisterRequest("Arman Zhaksylyk", "arman@mail.kz", "+77029876543", "Secret123");
        when(userRepository.findByEmail("arman@mail.kz")).thenReturn(Optional.of(new User()));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginWithWrongPasswordThrows401() {
        User user = new User();
        user.setEmail("arman@mail.kz");
        UserCredentials credentials = new UserCredentials();
        credentials.setUser(user);
        credentials.setPasswordHash("hashed-password");
        credentials.setRole("CUSTOMER");
        when(credentialsRepository.findByUserEmail("arman@mail.kz")).thenReturn(Optional.of(credentials));
        when(passwordEncoder.matches("WrongPass1", "hashed-password")).thenReturn(false);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("arman@mail.kz", "WrongPass1")));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtService, never()).createToken(any(), any());
    }
}

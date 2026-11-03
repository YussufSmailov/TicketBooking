package com.ticketbooking.unit;

import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findOrCreateReturnsExistingUser() {
        User existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setEmail("amina@gmail.com");

        when(userRepository.findByEmail("amina@gmail.com")).thenReturn(Optional.of(existingUser));

        User result = userService.findOrCreate("Amina Seitkali", "amina@gmail.com", "+77011234567");

        assertEquals("amina@gmail.com", result.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateSavesNewUser() {
        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreate("New User", "new@gmail.com", "+77000000000");

        assertEquals("New User", result.getFullName());
        assertEquals("new@gmail.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateRejectsInvalidEmail() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.findOrCreate("Yedige", "yedige@@mail,com", "+77000000000"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateRejectsEmptyName() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.findOrCreate("", "test@gmail.com", "+77000000000"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}

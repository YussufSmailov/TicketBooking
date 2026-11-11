package com.ticketbooking.unit;

import com.ticketbooking.dto.UserRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserWithExistingEmailThrows409() {
        when(userRepository.findByEmail("amina@gmail.com")).thenReturn(Optional.of(new User()));

        UserRequest request = new UserRequest("Amina Seitkali", "Amina@gmail.com", "+77011234567");
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> userService.create(request));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }
}

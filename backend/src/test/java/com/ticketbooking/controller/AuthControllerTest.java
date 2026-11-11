package com.ticketbooking.controller;

import com.ticketbooking.dto.AuthResponse;
import com.ticketbooking.dto.UserResponse;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.security.SecurityConfig;
import com.ticketbooking.service.AuthService;
import com.ticketbooking.service.CardService;
import com.ticketbooking.service.MyBookingsService;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, MeController.class})
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CardService cardService;

    @MockBean
    private MyBookingsService myBookingsService;

    @MockBean
    private UserService userService;

    @Test
    void registerWithWeakPasswordReturns400() throws Exception {
        String body = """
                {"fullName": "Dana Nurlanovna", "email": "dana@gmail.com", "phone": "+77075554433", "password": "onlyletters"}
                """;

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void loginReturnsTokenAndUser() throws Exception {
        UserResponse user = new UserResponse(3L, "Dana Nurlanovna", "dana@gmail.com", "+77075554433", "CUSTOMER");
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token", user));

        String body = """
                {"email": "dana@gmail.com", "password": "Secret123"}
                """;

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "dana@gmail.com")
    void meWithUserReturnsProfile() throws Exception {
        UserResponse user = new UserResponse(3L, "Dana Nurlanovna", "dana@gmail.com", "+77075554433", "CUSTOMER");
        when(authService.getCurrentUser("dana@gmail.com")).thenReturn(user);

        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("dana@gmail.com"));
    }
}

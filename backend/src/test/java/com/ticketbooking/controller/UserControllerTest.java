package com.ticketbooking.controller;

import com.ticketbooking.entity.User;
import com.ticketbooking.service.UserService;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @Test
    void wrongEmailReturns400AndUserIsNotSaved() throws Exception {
        String body = """
                {"fullName": "Amina Seitkali", "email": "amina@", "phone": "+77011234567"}
                """;

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(userService, never()).create(any());
    }

    @Test
    void wrongPhoneAndNameReturn400WithMessageForEachField() throws Exception {
        String body = """
                {"fullName": "A1", "email": "amina@gmail.com", "phone": "12345"}
                """;

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.phone").exists());
    }

    @Test
    void correctUserReturns201() throws Exception {
        User saved = new User();
        saved.setUserId(10L);
        saved.setFullName("Amina Seitkali");
        saved.setEmail("amina2@gmail.com");
        when(userService.create(any())).thenReturn(saved);

        String body = """
                {"fullName": "Amina Seitkali", "email": "amina2@gmail.com", "phone": "+77011234567"}
                """;

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotSeeAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}

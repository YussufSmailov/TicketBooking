package com.ticketbooking.controller;

import com.ticketbooking.dto.PaymentResult;
import com.ticketbooking.security.JwtService;
import com.ticketbooking.security.SecurityConfig;
import com.ticketbooking.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PaymentService paymentService;

    @Test
    void paymentWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/payments/cinema/1").contentType(MediaType.APPLICATION_JSON).content("{\"cardId\": 1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "amina@gmail.com")
    void wrongCvvReturns400() throws Exception {
        String body = """
                {"newCard": {"cardNumber": "4242424242424242", "holderName": "AMINA", "expMonth": 12, "expYear": 2030, "cvv": "1"}}
                """;

        mockMvc.perform(post("/api/payments/cinema/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors['newCard.cvv']").exists());

        verify(paymentService, never()).payCinemaBooking(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "amina@gmail.com")
    void concertPaymentGoesToConcertMethod() throws Exception {
        PaymentResult result = new PaymentResult("concert", 3L, new BigDecimal("12000"), "Paid", "Confirmed", "CON-000003", "ok");
        when(paymentService.payConcertBooking(eq(3L), eq("amina@gmail.com"), any())).thenReturn(result);

        mockMvc.perform(post("/api/payments/concert/3").contentType(MediaType.APPLICATION_JSON).content("{\"cardId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("CON-000003"));
    }
}

package com.ticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.service.EmailService;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Настоящие письма в тестах не отправляются: перехватываем код, который ушёл бы на почту.
     */
    @MockBean
    protected EmailService emailService;

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    protected String newEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@mail.kz";
    }

    protected void register(String fullName, String email) throws Exception {
        String registerBody = """
                {"fullName": "%s", "email": "%s", "phone": "+77010001122", "password": "Secret123"}
                """.formatted(fullName, email);
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated());
    }

    protected String sentCode(String email) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailService, org.mockito.Mockito.atLeastOnce()).sendVerificationCode(eq(email), any(), code.capture());
        return code.getValue();
    }

    protected String verifyEmail(String email, String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"code\": \"%s\"}".formatted(email, code)))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).get("token").asText();
    }

    /**
     * Регистрация → код из «письма» → подтверждение. Возвращает JWT.
     */
    protected String registerAndLogin(String fullName) throws Exception {
        String email = newEmail();
        register(fullName, email);
        return verifyEmail(email, sentCode(email));
    }
}

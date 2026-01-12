package ru.netology.cloudStorage.controller;

import ru.netology.cloudStorage.exception.BadCredentialsException;
import ru.netology.cloudStorage.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testLoginSuccess() throws Exception {

        when(authService.authenticate(any())).thenReturn("test-token");

        String loginRequest = "{\"login\": \"user1\", \"password\": \"password123\"}";

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth-token").value("test-token"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        when(authService.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        String loginRequest = "{\"login\": \"user1\", \"password\": \"wrong\"}";

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk());
    }
}

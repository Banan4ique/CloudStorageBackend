package ru.netology.cloudStorage.config;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ru.netology.cloudStorage.exception.BadCredentialsException;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (authException instanceof BadCredentialsException ex) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());

            Map<String, Object> errorResponse = new HashMap<>();

            Map<String, List<String>> fieldErrors = ex.getFieldErrors();

            List<String> emailErrors = fieldErrors.getOrDefault("email", new ArrayList<>());
            List<String> passwordErrors = fieldErrors.getOrDefault("password", new ArrayList<>());

            if (emailErrors.isEmpty() && passwordErrors.isEmpty()) {
                emailErrors = List.of("Неверный email или пароль");
                passwordErrors = List.of("Неверный email или пароль");
            }

            errorResponse.put("email", emailErrors);
            errorResponse.put("password", passwordErrors);

            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            log.debug("Sending error response: {}", jsonResponse);
            response.getWriter().write(jsonResponse);

        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("email", List.of("Ошибка авторизации"));
            errorResponse.put("password", List.of("Ошибка авторизации"));

            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
        }
    }
}
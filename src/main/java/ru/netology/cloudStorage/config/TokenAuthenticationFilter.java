package ru.netology.cloudStorage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.netology.cloudStorage.service.TokenService;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.debug("Processing {} {}", method, requestURI);

        if (isPublicEndpoint(requestURI, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("auth-token");

        if (token != null && !token.trim().isEmpty()) {
            token = token.trim();

            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }

            log.debug("Validating token: {}", token);

            try {
                Optional<UserDetails> userDetailsOpt = tokenService.validateToken(token);

                if (userDetailsOpt.isPresent()) {
                    UserDetails userDetails = userDetailsOpt.get();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.info("User authenticated: {}", userDetails.getUsername());
                } else {
                    log.warn("Token validation failed for request: {}", requestURI);
                }
            } catch (Exception e) {
                log.error("Token validation error for request {}: {}",
                        requestURI, e.getMessage(), e);
            }
        } else {
            log.debug("No auth-token header for request: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri, String method) {
        return (uri.equals("/login") && method.equals("POST")) ||
                (uri.equals("/health") && method.equals("GET")) ||
                (uri.equals("/info") && method.equals("GET")) ||
                method.equals("OPTIONS");
    }
}
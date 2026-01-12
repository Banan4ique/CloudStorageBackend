package ru.netology.cloudStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloudStorage.exception.BadCredentialsException;
import ru.netology.cloudStorage.DTO.LoginRequest;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public String authenticate(LoginRequest request) {
        log.info("Authentication attempt for: {}", request.getLogin());

        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getLogin());
                    throw BadCredentialsException.emailNotFound();
                });

        log.info("User found: id={}, login={}", user.getId(), user.getLogin());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BadCredentialsException.wrongPassword();
        }

        String token = tokenService.generateToken(user);
        log.info("Token generated for user: {}", user.getLogin());

        return token;
    }

    @Transactional
    public void logout(String token) {
        log.info("Logout request for token");
        tokenService.invalidateToken(token);
    }
}
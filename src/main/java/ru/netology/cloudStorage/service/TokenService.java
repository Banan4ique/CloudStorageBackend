package ru.netology.cloudStorage.service;

import ru.netology.cloudStorage.config.StorageProperties;
import ru.netology.cloudStorage.entity.AuthToken;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TokenService {

    private final AuthTokenRepository tokenRepository;
    private final StorageProperties storageProperties;
    private final int MAX_TOKENS_PER_USER = 5;

    /**
     * Генерация нового токена для пользователя.
     * Удаляет все старые токены пользователя перед созданием нового.
     */
    @Transactional
    public String generateToken(User user) {
        log.info("Generating token for user: {}", user.getLogin());

        tokenRepository.deleteByUser(user);
        log.debug("Deleted old tokens for user: {}", user.getLogin());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusHours(storageProperties.getToken().getValidityHours());

        AuthToken authToken = new AuthToken(user, token, expiresAt);
        tokenRepository.save(authToken);

        log.info("Generated new token for user: {}, expires at: {}",
                user.getLogin(), expiresAt);

        return token;
    }

    /**
     * Валидация токена.
     * Возвращает UserDetails если токен валиден.
     */
    @Transactional(readOnly = true)
    public Optional<UserDetails> validateToken(String token) {
        log.debug("Validating token: {}", token);

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
                log.debug("Token after removing Bearer: {}", token);
            }

            Optional<AuthToken> authTokenOpt = tokenRepository.findByTokenWithUser(token);

            if (authTokenOpt.isEmpty()) {
                log.debug("Token not found in database: {}", token);
                return Optional.empty();
            }

            AuthToken authToken = authTokenOpt.get();

            if (authToken.getUser() == null) {
                log.error("User is null for token: {}", token);
                return Optional.empty();
            }

            if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.debug("Token expired: {}, expires at: {}",
                        token, authToken.getExpiresAt());
                return Optional.empty();
            }

            User user = authToken.getUser();
            log.debug("Token valid for user: {}", user.getLogin());

            return Optional.of(user);

        } catch (Exception e) {
            log.error("Token validation error for token {}: {}", token, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Удаление токена при logout.
     */
    @Transactional
    public void invalidateToken(String token) {
        log.info("Invalidating token: {}", token);
        int deleted = tokenRepository.deleteByToken(token);

        if (deleted > 0) {
            log.debug("Token successfully invalidated: {}", token);
        } else {
            log.warn("Token not found for invalidation: {}", token);
        }
    }

    /**
     * Очистка просроченных токенов по расписанию.
     * Выполняется каждый час.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<AuthToken> expiredTokens = tokenRepository.findByExpiresAtBefore(now);

        if (!expiredTokens.isEmpty()) {
            tokenRepository.deleteAll(expiredTokens);
            log.info("Scheduled cleanup: deleted {} expired tokens", expiredTokens.size());
        }

        cleanupExcessiveTokens();
    }

    /**
     * Очистка лишних токенов (если у пользователя больше 5 активных токенов).
     */
    @Transactional
    public void cleanupExcessiveTokens() {
        List<Object[]> userTokenCounts = tokenRepository.findUsersWithTokenCount();

        for (Object[] result : userTokenCounts) {
            Long userId = (Long) result[0];
            Long tokenCount = (Long) result[1];

            if (tokenCount > MAX_TOKENS_PER_USER) {
                List<AuthToken> userTokens = tokenRepository.findByUserIdOrderByCreatedAtAsc(userId);
                List<AuthToken> tokensToDelete = userTokens.subList(0, userTokens.size() - MAX_TOKENS_PER_USER);

                if (!tokensToDelete.isEmpty()) {
                    tokenRepository.deleteAll(tokensToDelete);
                    log.debug("Deleted {} excessive tokens for user id: {}",
                            tokensToDelete.size(), userId);
                }
            }
        }
    }
}
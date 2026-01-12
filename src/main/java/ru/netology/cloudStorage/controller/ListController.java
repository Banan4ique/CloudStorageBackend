package ru.netology.cloudStorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import ru.netology.cloudStorage.DTO.FileInfoResponse;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.repository.UserRepository;
import ru.netology.cloudStorage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
@Slf4j
public class ListController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<FileInfoResponse>> listFiles(
            @RequestHeader("auth-token") String token,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @AuthenticationPrincipal Object principal) {

        log.info("List files request, limit: {}", limit);

        try {
            User user = extractUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (limit <= 0 || limit > 100) {
                log.warn("Invalid limit value: {}, using default 10", limit);
                limit = 10;
            }

            List<FileInfoResponse> files = fileStorageService.listFiles(user, limit);
            log.info("Returning {} files for user: {}", files.size(), user.getLogin());
            return ResponseEntity.ok(files);

        } catch (Exception e) {
            log.error("Error getting file list: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Безопасное извлечение User entity из Spring Security principal
     */
    private User extractUserFromPrincipal(Object principal) {
        if (principal == null) {
            log.error("Principal is null");
            return null;
        }

        log.debug("Principal type: {}", principal.getClass().getName());

        if (principal instanceof User) {
            User user = (User) principal;
            log.debug("Principal is User entity: {}", user.getLogin());
            return user;
        }

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            log.debug("Principal is UserDetails with username: {}", username);

            return userRepository.findByLogin(username)
                    .orElseThrow(() -> {
                        log.error("User not found in DB for username: {}", username);
                        return new RuntimeException("User not found");
                    });
        }

        if (principal instanceof String) {
            String username = (String) principal;
            log.debug("Principal is String username: {}", username);

            return userRepository.findByLogin(username)
                    .orElseThrow(() -> {
                        log.error("User not found in DB for username: {}", username);
                        return new RuntimeException("User not found");
                    });
        }

        log.error("Unknown principal type: {}", principal.getClass().getName());
        return null;
    }
}
package ru.netology.cloudStorage.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ru.netology.cloudStorage.DTO.RenameRequest;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.repository.UserRepository;
import ru.netology.cloudStorage.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Object principal) {

        log.info("Upload request for file: {}", filename);

        try {
            User user = extractUserFromPrincipal(principal);
            if (user == null) {
                log.error("Cannot extract user from principal");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            log.info("Uploading file: {} by user: {} (size: {} bytes)",
                    filename, user.getLogin(), file.getSize());

            fileStorageService.store(file, filename, user);
            log.info("File uploaded successfully: {}", filename);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters for {}: {}", filename, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("File upload failed for {}: {}", filename, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("IO error during file upload for {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<Resource> downloadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            @AuthenticationPrincipal Object principal) {

        log.info("Download request for file: {}", filename);

        try {
            User user = extractUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Path filePath = fileStorageService.load(filename, user);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", filename);
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + filename + "\"";

            log.info("File downloaded successfully: {} by user: {}", filename, user.getLogin());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (RuntimeException e) {
            log.error("File download failed for {}: {}", filename, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            log.error("Malformed URL for file {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        } catch (IOException e) {
            log.error("IO error during file download for {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            @AuthenticationPrincipal Object principal) {

        log.info("Delete request for file: {}", filename);

        try {
            User user = extractUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            fileStorageService.delete(filename, user);
            log.info("File deleted successfully: {} by user: {}", filename, user.getLogin());
            return ResponseEntity.ok().build();

        } catch (RuntimeException e) {
            log.error("File delete failed for {}: {}", filename, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("IO error during file delete for {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> renameFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String filename,
            @Valid @RequestBody RenameRequest request,
            @AuthenticationPrincipal Object principal) {

        log.info("Rename request for file: {} -> {}", filename, request.getName());

        try {
            User user = extractUserFromPrincipal(principal);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            fileStorageService.rename(filename, request.getName(), user);
            log.info("File renamed successfully: {} -> {} by user: {}",
                    filename, request.getName(), user.getLogin());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters for rename {} -> {}: {}",
                    filename, request.getName(), e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.error("File rename failed for {} to {}: {}",
                    filename, request.getName(), e.getMessage());
            return ResponseEntity.badRequest().build();
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
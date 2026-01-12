package ru.netology.cloudStorage.service;

import ru.netology.cloudStorage.repository.FileRepository;
import ru.netology.cloudStorage.config.StorageProperties;
import ru.netology.cloudStorage.entity.File;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.DTO.FileInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileRepository fileRepository;
    private final StorageProperties storageProperties;
    private Path rootLocation;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(storageProperties.getPath()).toAbsolutePath().normalize();
            Files.createDirectories(rootLocation);
            log.info("File storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Transactional
    public void store(MultipartFile file, String filename, User user) throws IOException {
        validateFilename(filename);

        if (fileRepository.existsByUserAndFilename(user, filename)) {
            throw new RuntimeException("File already exists: " + filename);
        }

        String uniqueFilename = generateUniqueFilename(filename);
        Path destinationFile = rootLocation.resolve(uniqueFilename);

        Files.copy(file.getInputStream(), destinationFile);

        File fileEntity = new File(filename, file.getSize(), uniqueFilename, user);
        fileRepository.save(fileEntity);
        log.info("File stored: {} for user: {}", filename, user.getLogin());
    }

    @Transactional(readOnly = true)
    public Path load(String filename, User user) {
        File file = fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new RuntimeException("File not found: " + filename));
        return rootLocation.resolve(file.getFilePath());
    }

    @Transactional
    public void delete(String filename, User user) throws IOException {
        File file = fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new RuntimeException("File not found: " + filename));

        Path filePath = rootLocation.resolve(file.getFilePath());
        Files.deleteIfExists(filePath);
        fileRepository.delete(file);

        log.info("File deleted: {} for user: {}", filename, user.getLogin());
    }

    @Transactional
    public void rename(String oldFilename, String newFilename, User user) {
        validateFilename(newFilename);

        File file = fileRepository.findByUserAndFilename(user, oldFilename)
                .orElseThrow(() -> new RuntimeException("File not found: " + oldFilename));

        if (fileRepository.existsByUserAndFilename(user, newFilename)) {
            throw new RuntimeException("File already exists: " + newFilename);
        }

        file.setFilename(newFilename);
        fileRepository.save(file);

        log.info("File renamed: {} -> {} for user: {}", oldFilename, newFilename, user.getLogin());
    }

    @Transactional(readOnly = true)
    public List<FileInfoResponse> listFiles(User user, int limit) {
        return fileRepository.findAllByUserOrderByUploadDateDesc(user).stream()
                .limit(limit)
                .map(file -> new FileInfoResponse(file.getFilename(), file.getSize()))
                .collect(Collectors.toList());
    }

    private void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new RuntimeException("Filename cannot be empty");
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new RuntimeException("Invalid filename: " + filename);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID() + extension;
    }
}
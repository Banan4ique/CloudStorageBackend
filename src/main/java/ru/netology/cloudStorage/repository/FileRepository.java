package ru.netology.cloudStorage.repository;

import ru.netology.cloudStorage.entity.File;
import ru.netology.cloudStorage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByUserAndFilename(User user, String filename);
    List<File> findAllByUserOrderByUploadDateDesc(User user);
    boolean existsByUserAndFilename(User user, String filename);
    void deleteByUserAndFilename(User user, String filename);
}
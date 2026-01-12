package ru.netology.cloudStorage.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Data
@NoArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String filename;

    private Long size;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    public File(String filename, Long size, String filePath, User user) {
        this.filename = filename;
        this.size = size;
        this.filePath = filePath;
        this.user = user;
        this.uploadDate = LocalDateTime.now();
    }
}
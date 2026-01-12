package ru.netology.cloudStorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "ru.netology.cloudStorage.config",
                "ru.netology.cloudStorage.controller",
                "ru.netology.cloudStorage.service",
                "ru.netology.cloudStorage.repository"
        }
)
@EntityScan("ru.netology.cloudStorage.entity")
@EnableJpaRepositories("ru.netology.cloudStorage.repository")
public class CloudStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudStorageApplication.class, args);
    }
}
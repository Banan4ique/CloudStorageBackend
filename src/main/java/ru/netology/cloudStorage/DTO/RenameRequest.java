package ru.netology.cloudStorage.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RenameRequest {
    @NotBlank(message = "New filename is required")
    @JsonAlias({"name", "filename"})
    private String name;
}
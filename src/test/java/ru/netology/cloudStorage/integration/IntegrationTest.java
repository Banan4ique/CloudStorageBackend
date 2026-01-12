package ru.netology.cloudStorage.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFullFlow() throws Exception {
        // 1. Login
        String loginRequest = "{\"login\": \"user1\", \"password\": \"password123\"}";

        String token = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auth-token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = token.split("\"auth-token\":\"")[1].split("\"")[0];

        // 2. Upload
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(file)
                        .param("name", "test.txt")
                        .header("auth-token", token))
                .andExpect(status().isOk());

        // 3. List
        mockMvc.perform(get("/file/list")
                        .header("auth-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test.txt"));

        // 4. Logout
        mockMvc.perform(post("/logout")
                        .header("auth-token", token))
                .andExpect(status().isOk());
    }
}

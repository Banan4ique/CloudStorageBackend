package ru.netology.cloudStorage.controller;

import ru.netology.cloudStorage.config.StorageProperties;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private StorageProperties storageProperties;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin("testuser");

        when(storageProperties.getPath()).thenReturn("./test-uploads");
        when(storageProperties.getToken()).thenReturn(new StorageProperties.TokenConfig());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(file)
                        .param("name", "test.txt")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        verify(fileStorageService).store(any(), eq("test.txt"), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDownloadFile() throws Exception {
        Path mockPath = Path.of("test.txt");
        when(fileStorageService.load("test.txt", testUser)).thenReturn(mockPath);

        mockMvc.perform(get("/file")
                        .param("name", "test.txt")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testDeleteFile() throws Exception {
        mockMvc.perform(delete("/file")
                        .param("name", "test.txt")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk());

        verify(fileStorageService).delete("test.txt", testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRenameFile() throws Exception {
        mockMvc.perform(put("/file")
                        .param("name", "old.txt")
                        .header("auth-token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"new.txt\"}"))
                .andExpect(status().isOk());

        verify(fileStorageService).rename("old.txt", "new.txt", testUser);
    }

    @Test
    @WithMockUser(username = "testuser")
    void testListFiles() throws Exception {
        when(fileStorageService.listFiles(testUser, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/file/list")
                        .param("limit", "10")
                        .header("auth-token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

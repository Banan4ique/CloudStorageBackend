package ru.netology.cloudStorage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud.storage")
@Data
public class StorageProperties {

    private String path;
    private TokenConfig token = new TokenConfig();

    @Data
    public static class TokenConfig {
        private int validityHours = 24;
        private int maxTokensPerUser = 5;
        private String secretKey;
    }
}

package com.b1.module.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "b1.ai")
public class AiConfigProperties {

    private ProviderConfig deepseek;
    private ProviderConfig qwen;

    @Data
    public static class ProviderConfig {
        private String apiKey;
        private String baseUrl;
        private String model;
    }
}

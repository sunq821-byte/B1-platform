package com.b1.module.ai.provider.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiRequest {

    private String systemPrompt;
    private String userContent;
    private List<String> imageUrls;
    private boolean vision;

    public static AiRequest textOnly(String systemPrompt, String userContent) {
        return AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userContent(userContent)
                .vision(false)
                .build();
    }

    public static AiRequest vision(String systemPrompt, String userContent, List<String> imageUrls) {
        return AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userContent(userContent)
                .imageUrls(imageUrls)
                .vision(true)
                .build();
    }
}

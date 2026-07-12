package com.b1.module.ai.provider;

import com.b1.module.ai.config.AiConfigProperties;
import com.b1.module.ai.provider.dto.AiRequest;
import com.b1.module.ai.provider.dto.AiResponse;
import com.b1.module.ai.provider.dto.AiResponse.DimensionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekProvider implements AiProvider {

    private final AiConfigProperties config;
    private final ObjectMapper objectMapper;
    private final AiHttpClient aiHttpClient;

    @Override
    public String getProviderName() {
        return "DEEPSEEK";
    }

    @Override
    public AiResponse analyze(AiRequest request) {
        long start = System.currentTimeMillis();

        AiConfigProperties.ProviderConfig cfg = config.getDeepseek();
        String url = cfg.getBaseUrl() + "/chat/completions";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", cfg.getModel());
        body.put("temperature", 0.1);
        body.put("max_tokens", 4096);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));
        messages.add(Map.of("role", "user", "content", request.getUserContent()));
        body.put("messages", messages);

        try {
            String reqJson = objectMapper.writeValueAsString(body);
            log.debug("DeepSeek request: {}", reqJson.substring(0, Math.min(500, reqJson.length())));

            String respBody = aiHttpClient.postJson(url, cfg.getApiKey(), reqJson);

            long duration = System.currentTimeMillis() - start;

            if (respBody != null && !respBody.isBlank()) {
                JsonNode root = objectMapper.readTree(respBody);
                JsonNode choices = root.path("choices");
                if (choices.isEmpty()) {
                    return buildErrorResponse("DeepSeek returned empty choices", duration);
                }

                String content = choices.get(0).path("message").path("content").asText();
                int inputTokens = root.path("usage").path("prompt_tokens").asInt();
                int outputTokens = root.path("usage").path("completion_tokens").asInt();
                int totalTokens = root.path("usage").path("total_tokens").asInt();

                return parseStructuredResponse(content, inputTokens, outputTokens, totalTokens, duration);
            }

            return buildErrorResponse("DeepSeek returned empty body", duration);

        } catch (JsonProcessingException e) {
            long duration = System.currentTimeMillis() - start;
            return buildErrorResponse("JSON serialization error: " + e.getMessage(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("DeepSeek API call failed", e);
            return buildErrorResponse("API error: " + e.getMessage(), duration);
        }
    }

    private AiResponse parseStructuredResponse(String content, int inputTokens, int outputTokens, int totalTokens, long duration) {
        try {
            String jsonContent = extractJson(content);
            JsonNode root = objectMapper.readTree(jsonContent);

            List<DimensionResult> dimensions = new ArrayList<>();
            JsonNode dimsNode = root.path("dimensions");
            if (dimsNode.isArray()) {
                for (JsonNode dim : dimsNode) {
                    dimensions.add(DimensionResult.builder()
                            .dimensionName(dim.path("dimension_name").asText())
                            .dimensionCode(dim.path("dimension_code").asText())
                            .issueType(dim.path("issue_type").asText())
                            .severity(dim.path("severity").asText("INFO"))
                            .suggestDeduct(new BigDecimal(dim.path("suggest_deduct").asText("0")))
                            .reason(dim.path("reason").asText())
                            .suggestion(dim.path("suggestion").asText())
                            .lineNumber(dim.path("line_number").isNull() ? null : dim.path("line_number").asInt())
                            .confidence(new BigDecimal(dim.path("confidence").asText("0.8")))
                            .build());
                }
            }

            List<String> strengths = new ArrayList<>();
            for (JsonNode s : root.path("strengths")) {
                strengths.add(s.asText());
            }
            List<String> weaknesses = new ArrayList<>();
            for (JsonNode w : root.path("weaknesses")) {
                weaknesses.add(w.asText());
            }

            return AiResponse.builder()
                    .success(true)
                    .rawContent(content)
                    .overallScore(new BigDecimal(root.path("overall_score").asText("0")))
                    .summary(root.path("summary").asText())
                    .dimensions(dimensions)
                    .strengths(strengths)
                    .weaknesses(weaknesses)
                    .improvementPlan(root.path("improvement_plan").asText(""))
                    .tokenInput(inputTokens)
                    .tokenOutput(outputTokens)
                    .tokenTotal(totalTokens)
                    .modelUsed(config.getDeepseek().getModel())
                    .durationMs(duration)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse DeepSeek response", e);
            return AiResponse.builder()
                    .success(true)
                    .rawContent(content)
                    .overallScore(BigDecimal.ZERO)
                    .summary(content)
                    .dimensions(Collections.emptyList())
                    .strengths(Collections.emptyList())
                    .weaknesses(Collections.emptyList())
                    .improvementPlan("")
                    .tokenInput(inputTokens)
                    .tokenOutput(outputTokens)
                    .tokenTotal(totalTokens)
                    .modelUsed(config.getDeepseek().getModel())
                    .durationMs(duration)
                    .build();
        }
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private AiResponse buildErrorResponse(String error, long duration) {
        return AiResponse.builder()
                .success(false)
                .rawContent(error)
                .overallScore(BigDecimal.ZERO)
                .summary(error)
                .dimensions(Collections.emptyList())
                .strengths(Collections.emptyList())
                .weaknesses(Collections.emptyList())
                .improvementPlan("")
                .durationMs(duration)
                .build();
    }
}

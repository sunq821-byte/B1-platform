package com.b1.module.ai.provider.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AiResponse {

    private boolean success;
    private String rawContent;
    private BigDecimal overallScore;
    private String summary;
    private List<DimensionResult> dimensions;
    private List<String> strengths;
    private List<String> weaknesses;
    private String improvementPlan;
    private Integer tokenInput;
    private Integer tokenOutput;
    private Integer tokenTotal;
    private String modelUsed;
    private long durationMs;

    @Data
    @Builder
    public static class DimensionResult {
        private String dimensionName;
        private String dimensionCode;
        private String issueType;
        private String severity;
        private BigDecimal suggestDeduct;
        private String reason;
        private String suggestion;
        private Integer lineNumber;
        private BigDecimal confidence;
    }
}

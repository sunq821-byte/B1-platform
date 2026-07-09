package com.b1.module.ai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiResultVO {

    private Long analyzeId;

    private String status;

    private Integer progress;

    private String currentDimension;

    private AiScoreResultVO result;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Data
    public static class AiScoreResultVO {

        private BigDecimal overallScore;

        private List<AiDimensionScoreVO> dimensions;

        private String summary;

        private List<String> strengths;

        private List<String> weaknesses;

        private String improvementPlan;
    }

    @Data
    public static class AiDimensionScoreVO {

        private String dimensionName;

        private BigDecimal score;

        private BigDecimal maxScore;

        private BigDecimal weight;

        private String comment;

        private List<String> suggestions;

        private List<String> codeReferences;

        private String agentType;

        private String issueType;

        private BigDecimal suggestDeduct;

        private String filePath;

        private Integer lineNumber;

        private BigDecimal confidence;
    }
}
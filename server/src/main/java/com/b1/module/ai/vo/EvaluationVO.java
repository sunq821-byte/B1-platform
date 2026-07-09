package com.b1.module.ai.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvaluationVO {

    private Long submissionId;

    private Long taskId;

    private String taskName;

    private String courseName;

    private LocalDateTime submittedAt;

    private AiEvalSummaryVO aiEvaluation;

    private TeacherEvalSummaryVO teacherEvaluation;

    private String status;

    private BigDecimal finalScore;

    private String rejectReason;

    @Data
    public static class AiEvalSummaryVO {

        private BigDecimal overallScore;

        private String summary;

        private LocalDateTime completedAt;
    }

    @Data
    public static class TeacherEvalSummaryVO {

        private BigDecimal overallScore;

        private String comment;

        private List<TeacherDimensionScoreVO> dimensions;

        private String scoredBy;

        private LocalDateTime scoredAt;

        private LocalDateTime publishedAt;
    }

    @Data
    public static class TeacherDimensionScoreVO {

        private String dimensionName;

        private BigDecimal score;

        private BigDecimal maxScore;
    }
}
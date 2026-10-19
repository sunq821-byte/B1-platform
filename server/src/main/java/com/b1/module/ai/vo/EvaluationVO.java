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

    /** 学生提交方式：ZIP_UPLOAD / GIT_URL / ONLINE_CODE */
    private String submitType;

    /** 主附件文件名，用于前端推断 AI 分析维度 */
    private String attachmentFileName;

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
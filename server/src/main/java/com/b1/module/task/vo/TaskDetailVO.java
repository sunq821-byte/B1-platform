package com.b1.module.task.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailVO {

    private Long taskId;

    private String taskName;

    private String courseName;

    private String teacherName;

    private String teacherEmail;

    private String description;

    private LocalDateTime deadline;

    private BigDecimal totalScore;

    private String submissionType;

    private Integer submitLimit;

    private List<EvaluationDimensionVO> evaluationDimensions;

    private List<AttachmentVO> attachments;

    private String mySubmissionStatus;

    private Integer mySubmitCount;

    private Integer maxSubmitCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    public static class EvaluationDimensionVO {

        private String dimensionName;

        private BigDecimal weight;

        private BigDecimal maxScore;
    }

    @Data
    public static class AttachmentVO {

        private Long fileId;

        private String fileName;

        private Long fileSize;

        private String fileType;
    }
}
package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmissionDetailVO {

    private Long submissionId;

    private String taskName;

    private Long taskId;

    private String studentName;

    private Long studentUserId;

    private String submitType;

    private String status;

    private String gitUrl;

    private String gitBranch;

    private String summary;

    private Integer submitCount;

    private Integer isLate;

    private LocalDateTime submittedAt;

    private List<AttachmentVO> attachments;

    private TeacherReviewVO review;

    private List<AiDimensionVO> aiDimensions;

    @Data
    public static class AttachmentVO {

        private Long fileId;

        private String fileName;

        private Long fileSize;

        private String fileType;

        private String downloadUrl;
    }

    @Data
    public static class TeacherReviewVO {

        private Long reviewId;

        private BigDecimal totalScore;

        private String comment;

        private String status;

        private LocalDateTime reviewTime;
    }

    @Data
    public static class AiDimensionVO {

        private String dimensionName;

        private BigDecimal score;

        private String comment;
    }
}
package com.b1.module.teacher.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionListVO {

    private Long submissionId;

    private String taskName;

    private Long taskId;

    private Long studentUserId;

    private String studentName;

    private String studentEmail;

    private String submitType;

    private String status;

    private Integer submitCount;

    private Integer isLate;

    private LocalDateTime submittedAt;

    private Boolean hasReview;
}
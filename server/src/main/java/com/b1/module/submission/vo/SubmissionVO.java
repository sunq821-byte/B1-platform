package com.b1.module.submission.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionVO {

    private Long submissionId;

    private Long taskId;

    private String submissionType;

    private String status;

    private Integer submitCount;

    private Integer maxSubmitCount;

    private LocalDateTime submittedAt;
}
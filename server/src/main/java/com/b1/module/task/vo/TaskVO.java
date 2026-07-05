package com.b1.module.task.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskVO {

    private Long taskId;

    private String taskName;

    private String courseName;

    private String teacherName;

    private LocalDateTime deadline;

    private BigDecimal totalScore;

    private String submissionType;

    private String status;

    private String mySubmissionStatus;

    private LocalDateTime createdAt;
}
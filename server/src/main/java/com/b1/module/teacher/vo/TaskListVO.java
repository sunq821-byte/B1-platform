package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskListVO {

    private Long taskId;

    private String taskName;

    private String courseName;

    private String submissionType;

    private Integer maxSubmitCount;

    private BigDecimal totalScore;

    private String status;

    private LocalDateTime deadline;

    private LocalDateTime publishTime;

    private Integer submissionCount;

    private Integer reviewedCount;
}
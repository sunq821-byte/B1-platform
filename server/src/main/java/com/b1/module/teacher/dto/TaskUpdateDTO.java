package com.b1.module.teacher.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskUpdateDTO {

    private String taskName;

    private String description;

    private String requirement;

    private String submissionType;

    private Integer maxSubmitCount;

    private Integer allowLate;

    private LocalDateTime endTime;

    private BigDecimal totalScore;

    private Long standardId;
}
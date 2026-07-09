package com.b1.module.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskCreateDTO {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotBlank(message = "提交方式不能为空")
    private String submissionType;

    @NotNull(message = "最大提交次数不能为空")
    private Integer maxSubmitCount;

    @NotNull(message = "总分不能为空")
    private BigDecimal totalScore;

    private String description;

    private String requirement;

    private LocalDateTime endTime;

    private Integer allowLate;

    private Long standardId;

    private Long trainingClassId;

    private Long courseId;
}
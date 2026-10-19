package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailVO {

    private Long taskId;

    private String taskName;

    private String courseName;

    private String description;

    private String requirement;

    private String submissionType;

    private Integer maxSubmitCount;

    private Integer allowLate;

    private LocalDateTime endTime;

    private LocalDateTime publishTime;

    private BigDecimal totalScore;

    private String status;

    private String gradingRule;

    private Long standardId;

    private String standardName;

    private List<TaskStudentVO> students;

    @Data
    public static class TaskStudentVO {

        private Long userId;

        private String realName;

        private String email;

        private Long submissionId;

        private Integer submitCount;

        private Integer aiAnalyzeUsedCount;

        private Integer maxAiAnalyzeCount;

        private String latestStatus;

        private LocalDateTime latestSubmitTime;

        private Boolean hasReview;
    }
}
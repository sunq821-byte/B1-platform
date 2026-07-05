package com.b1.module.course.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseDetailVO {

    private Long courseId;

    private String courseName;

    private String courseCode;

    private String teacherName;

    private String teacherEmail;

    private String semester;

    private BigDecimal credits;

    private String description;

    private List<CourseTaskVO> tasks;

    @Data
    public static class CourseTaskVO {

        private Long taskId;

        private String taskName;

        private LocalDateTime deadline;

        private BigDecimal totalScore;

        private String mySubmissionStatus;

        private BigDecimal myScore;
    }
}
package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseDetailVO {

    private Long courseId;

    private String courseName;

    private String courseCode;

    private String semester;

    private BigDecimal credits;

    private String description;

    private String syllabus;

    private String objectives;

    private String status;

    private Integer studentCount;

    private List<TeacherVO> teachers;

    private List<TaskSummaryVO> tasks;

    @Data
    public static class TeacherVO {

        private Long userId;

        private String realName;

        private String email;
    }

    @Data
    public static class TaskSummaryVO {

        private Long taskId;

        private String taskName;

        private String status;

        private LocalDateTime deadline;

        private Integer submissionCount;
    }
}
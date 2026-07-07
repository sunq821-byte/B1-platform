package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherDashboardVO {

    private Integer totalCourses;

    private Integer totalStudents;

    private Integer totalTasks;

    private Integer pendingReviewCount;

    private Integer publishedTaskCount;

    private List<CourseStatVO> courseStats;

    private List<RecentSubmissionVO> recentSubmissions;

    @Data
    public static class CourseStatVO {

        private Long courseId;

        private String courseName;

        private Integer studentCount;

        private Integer taskCount;

        private BigDecimal avgScore;

        private Integer submissionCount;
    }

    @Data
    public static class RecentSubmissionVO {

        private Long submissionId;

        private String taskName;

        private String studentName;

        private String courseName;

        private String status;

        private LocalDateTime submittedAt;
    }
}
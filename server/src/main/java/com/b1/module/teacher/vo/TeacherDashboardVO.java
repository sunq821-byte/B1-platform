package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherDashboardVO {

    private DashboardStatsVO stats;

    private List<PendingReviewVO> pendingReviews;

    private SubmitRateVO submitRateByClass;

    private List<UpcomingTaskVO> upcomingTasks;

    @Data
    public static class DashboardStatsVO {
        private Integer totalStudents;
        private Integer classCount;
        private Integer pendingCount;
        private Integer reviewedCount;
        private Integer submissionRate;
    }

    @Data
    public static class PendingReviewVO {
        private Long submissionId;
        private String studentName;
        private String taskName;
        private LocalDateTime submittedAt;
        private String status;
    }

    @Data
    public static class SubmitRateVO {
        private List<String> classNames;
        private List<Integer> values;
        private List<BigDecimal> avgScores;
    }

    @Data
    public static class UpcomingTaskVO {
        private Long taskId;
        private String taskName;
        private String courseName;
        private LocalDateTime deadline;
        private Integer unsubmittedCount;
        private Integer totalStudents;
    }
}

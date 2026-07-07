package com.b1.module.student.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardVO {

    private Integer totalCourses;

    private Integer totalSubmissions;

    private Integer pendingReviewCount;

    private Integer unreadNotificationCount;

    private List<TaskDeadlineVO> upcomingDeadlines;

    private List<RecentActivityVO> recentActivities;

    private BigDecimal averageScore;

    @Data
    public static class TaskDeadlineVO {

        private Long taskId;

        private String taskName;

        private String courseName;

        private LocalDateTime deadline;

        private Integer remainingDays;

        private String myStatus;
    }

    @Data
    public static class RecentActivityVO {

        private String type;

        private String title;

        private String description;

        private LocalDateTime occurredAt;
    }
}

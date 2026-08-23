package com.b1.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardVO {

    private Stats stats;
    private RuntimeSummary runtimeSummary;
    private ActivityTrend activityTrend;
    private PriorityAlert priorityAlert;
    private List<LogEntry> recentLogs;
    private List<ServiceHealth> health;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int totalUsers;
        private int studentCount;
        private int teacherCount;
        private int adminCount;
        private int todayActive;
        private double activeRate;
        private int activeCourses;
        private int totalSubmissions;
        private int completionRate;
        private int aiCallsToday;
        private double storageUsedGb;
        private double storageQuotaGb;
        private int alertCount24h;
        private double accountEnableRate;
        private int healthyServices;
        private int totalServices;
        private double aiQuotaRate;
        private double alertDeltaPct;
        private double aiCallsDeltaPct;
        private double completionDeltaPct;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuntimeSummary {
        private int healthy;
        private int degraded;
        private int down;
        private int aiQueue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityTrend {
        private List<String> xAxis;
        private List<Integer> activeUsers;
        private List<Integer> aiCalls;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityAlert {
        private String title;
        private String summary;
        private String priority;
        private String source;
        private String actionPath;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogEntry {
        private String type;
        private String message;
        private String detail;
        private String createdAt;
        private String operator;
        private String ip;
        private String result;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String name;
        private String status;
        private String color;
        private String detail;
        private Integer latencyMs;
    }
}

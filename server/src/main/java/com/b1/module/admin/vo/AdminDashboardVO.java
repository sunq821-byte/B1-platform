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
    private List<LogEntry> recentLogs;
    private List<ServiceHealth> health;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int totalUsers;
        private int activeCourses;
        private int totalSubmissions;
        private int completionRate;
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String name;
        private String status;
        private String color;
    }
}

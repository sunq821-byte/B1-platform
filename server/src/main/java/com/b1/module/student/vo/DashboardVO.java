package com.b1.module.student.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardVO {

    private StatsVO stats;

    private List<RecentTaskVO> recentTasks;

    private ScoreTrendVO scoreTrend;

    private RadarDataVO radarData;

    private List<String> aiSuggestions;

    @Data
    public static class StatsVO {
        private Integer pendingTasks;
        private Integer completedTasks;
        private BigDecimal averageScore;
        private BigDecimal classAverageScore;
        private Integer analyzingCount;
        private Integer improvementCount;
        private String nearestDeadline;
        private Integer analyzingEtaMinutes;
    }

    @Data
    public static class RecentTaskVO {
        private String taskId;
        private String taskName;
        private String courseName;
        private LocalDateTime deadline;
        private String status;
        private BigDecimal score;
        private Long submissionId;
    }

    @Data
    public static class ScoreTrendVO {
        private List<String> xAxis;
        private List<BigDecimal> myScores;
        private List<BigDecimal> classAvg;
    }

    @Data
    public static class RadarDataVO {
        private List<String> categories;
        private List<BigDecimal> myScores;
        private List<BigDecimal> classAvg;
    }
}

package com.b1.module.student.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportVO {

    private Stats stats;
    private ScoreTrend scoreTrend;
    private RadarData radarData;
    private List<Row> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int totalTasks;
        private int completedTasks;
        private BigDecimal averageScore;
        private int totalSubmissions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreTrend {
        private List<String> categories;
        private List<BigDecimal> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RadarData {
        private List<String> indicators;
        private List<BigDecimal> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private String taskName;
        private String courseName;
        private BigDecimal score;
        private String status;
        private String reviewComment;
    }
}

package com.b1.module.teacher.vo;

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
public class ClassReportVO {

    private Stats stats;
    private Histogram histogram;
    private CourseAvgs courseAvgs;
    private List<Row> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int totalStudents;
        private int totalReviewed;
        private BigDecimal classAverage;
        private BigDecimal passRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Histogram {
        private List<String> categories;
        private List<Integer> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseAvgs {
        private List<String> categories;
        private List<BigDecimal> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        private String studentId;
        private String name;
        private String className;
        private int completedCount;
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
    }
}

package com.b1.module.teacher.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private TaskCompletion taskCompletion;
    private DimensionAvgs dimensionAvgs;
    private List<Row> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private int totalStudents;
        private int totalReviewed;
        private int totalTasks;
        private BigDecimal classAverage;
        private BigDecimal passRate;
        private BigDecimal taskCompletionRate;
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
    public static class TaskCompletion {
        private List<String> categories;
        private List<Integer> completedCount;
        private List<Integer> totalStudents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DimensionAvgs {
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
        private String courseName;
        private int completedCount;
        private BigDecimal avgScore;
        private BigDecimal maxScore;
        private BigDecimal minScore;
    }
}

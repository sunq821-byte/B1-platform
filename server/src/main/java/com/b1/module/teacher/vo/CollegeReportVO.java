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
public class CollegeReportVO {

    private CrossClass crossClass;
    private SemesterTrend semesterTrend;
    private List<ClassDetail> classDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrossClass {
        private List<String> classNames;
        private List<BigDecimal> values;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterTrend {
        private List<String> semesters;
        private List<Series> series;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Series {
            private String name;
            private List<BigDecimal> data;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassDetail {
        private String className;
        private String courseName;
        private String semester;
        private int studentCount;
        private BigDecimal avgScore;
        private BigDecimal passRate;
        private BigDecimal maxScore;
    }
}

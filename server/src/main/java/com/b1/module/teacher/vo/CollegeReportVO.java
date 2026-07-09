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
public class CollegeReportVO {

    private CrossClass crossClass;
    private SemesterTrend semesterTrend;

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
}

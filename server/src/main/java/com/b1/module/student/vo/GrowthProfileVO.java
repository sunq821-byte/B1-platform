package com.b1.module.student.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GrowthProfileVO {

    private Integer totalTasks;

    private Integer completedTasks;

    private Integer totalSubmissions;

    private BigDecimal completionRate;

    private BigDecimal averageScore;

    private BigDecimal highestScore;

    private BigDecimal lowestScore;

    private List<CourseScoreVO> courseScores;

    private List<MonthlyTrendVO> monthlyTrends;

    private List<DimensionRadarVO> dimensionRadar;

    private DimensionInsightVO dimensionInsight;

    private List<GrowthTimelineItemVO> growthTimeline;

    private List<SubmissionHistoryVO> submissionHistory;

    @Data
    public static class CourseScoreVO {

        private Long courseId;

        private String courseName;

        private BigDecimal avgScore;

        private Integer taskCount;
    }

    @Data
    public static class MonthlyTrendVO {

        private String month;

        private BigDecimal avgScore;

        private Integer submissionCount;
    }

    @Data
    public static class DimensionRadarVO {

        private String dimensionName;

        private BigDecimal myAvg;

        private BigDecimal classAvg;
    }

    @Data
    public static class DimensionInsightVO {

        private String strongest;

        private String weakest;

        private String summary;
    }

    @Data
    public static class GrowthTimelineItemVO {

        private String date;

        private String title;

        private String description;

        /** submit | score | reject */
        private String type;

        private BigDecimal score;
    }

    @Data
    public static class SubmissionHistoryVO {

        private Long submissionId;

        private Long taskId;

        private String taskName;

        private String courseName;

        private BigDecimal score;

        private String result;

        private LocalDateTime submittedAt;
    }
}

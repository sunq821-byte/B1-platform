package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StandardDetailVO {

    private Long standardId;

    private String standardName;

    private String description;

    private String courseType;

    private String status;

    private Integer isTemplate;

    private List<DimensionVO> dimensions;

    private LocalDateTime createTime;

    @Data
    public static class DimensionVO {

        private Long dimensionId;

        private String dimName;

        private String dimDescription;

        private BigDecimal weight;

        private BigDecimal maxScore;

        private Integer sortOrder;
    }
}
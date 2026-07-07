package com.b1.module.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReviewSubmitDTO {

    @NotBlank(message = "批阅状态不能为空")
    private String status;

    private String teacherComment;

    private List<DimensionScoreDTO> dimensions;

    @Data
    public static class DimensionScoreDTO {

        @NotNull(message = "维度ID不能为空")
        private Long dimensionId;

        @NotNull(message = "分数不能为空")
        private BigDecimal score;

        private String remark;
    }
}
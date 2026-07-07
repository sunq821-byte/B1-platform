package com.b1.module.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StandardCreateDTO {

    @NotBlank(message = "标准名称不能为空")
    private String standardName;

    private String description;

    private String courseType;

    private Integer isTemplate;

    @NotEmpty(message = "评价维度不能为空")
    private List<DimensionItem> dimensions;

    @Data
    public static class DimensionItem {

        @NotBlank(message = "维度名称不能为空")
        private String dimName;

        private String dimDescription;

        @NotNull(message = "权重不能为空")
        private BigDecimal weight;

        @NotNull(message = "满分不能为空")
        private BigDecimal maxScore;

        private Integer sortOrder;
    }
}
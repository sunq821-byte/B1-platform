package com.b1.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_analysis")
public class AiAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @TableField("id")
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("total_score")
    private BigDecimal totalScore;

    @TableField("analysis_status")
    private String analysisStatus;

    @TableField("error_message")
    private String errorMessage;

    @TableField("analysis_time_ms")
    private Integer analysisTimeMs;

    @TableField("model_provider")
    private String modelProvider;

    @TableField("model_name")
    private String modelName;

    @TableField("token_input")
    private Integer tokenInput;

    @TableField("token_output")
    private Integer tokenOutput;

    @TableField("token_total")
    private Integer tokenTotal;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("raw_response")
    private String rawResponse;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @Version
    @TableField("version")
    private Integer version;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

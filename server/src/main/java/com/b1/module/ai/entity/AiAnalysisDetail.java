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
@TableName("ai_analysis_detail")
public class AiAnalysisDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @TableField("id")
    private Long id;

    @TableField("ai_analysis_id")
    private Long aiAnalysisId;

    @TableField("dimension_id")
    private Long dimensionId;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("agent_type")
    private String agentType;

    @TableField("file_path")
    private String filePath;

    @TableField("line_number")
    private Integer lineNumber;

    @TableField("issue_type")
    private String issueType;

    @TableField("severity")
    private String severity;

    @TableField("reason")
    private String reason;

    @TableField("suggestion")
    private String suggestion;

    @TableField("suggest_deduct")
    private BigDecimal suggestDeduct;

    @TableField("confidence")
    private BigDecimal confidence;

    @TableField("is_adopted")
    private Integer isAdopted;

    @TableField("adjusted_deduct")
    private BigDecimal adjustedDeduct;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

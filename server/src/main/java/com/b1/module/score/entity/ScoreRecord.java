package com.b1.module.score.entity;

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
@TableName("score_record")
public class ScoreRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("user_id")
    private Long userId;

    @TableField("training_task_id")
    private Long trainingTaskId;

    @TableField("total_score")
    private BigDecimal totalScore;

    @TableField("ai_total_score")
    private BigDecimal aiTotalScore;

    @TableField("score_details")
    private String scoreDetails;

    @TableField("teacher_comment")
    private String teacherComment;

    @TableField("status")
    private String status;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @Version
    @TableField("version")
    private Integer version;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

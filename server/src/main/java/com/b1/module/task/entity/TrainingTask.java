package com.b1.module.task.entity;

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
@TableName("training_task")
public class TrainingTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @TableField("id")
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("standard_id")
    private Long standardId;

    @TableField("task_name")
    private String taskName;

    @TableField("description")
    private String description;

    @TableField("requirement")
    private String requirement;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("allow_late")
    private Integer allowLate;

    @TableField("late_penalty")
    private BigDecimal latePenalty;

    @TableField("submission_type")
    private String submissionType;

    @TableField("max_submit_count")
    private Integer maxSubmitCount;

    @TableField("max_score")
    private BigDecimal maxScore;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("priority")
    private String priority;

    @TableField("status")
    private String status;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("end_actual_time")
    private LocalDateTime endActualTime;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @Version
    @TableField("version")
    private Integer version;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
}

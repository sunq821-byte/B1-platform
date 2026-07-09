package com.b1.module.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("teacher_review")
public class TeacherReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("teacher_comment")
    private String teacherComment;

    @TableField("status")
    private String status;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField("publish_time")
    private LocalDateTime publishTime;

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

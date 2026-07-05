package com.b1.module.submission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("submission")
public class Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @TableField("id")
    private Long id;

    @TableField("training_task_id")
    private Long trainingTaskId;

    @TableField("user_id")
    private Long userId;

    @TableField("submit_type")
    private String submitType;

    @TableField("git_url")
    private String gitUrl;

    @TableField("git_branch")
    private String gitBranch;

    @TableField("git_commit_id")
    private String gitCommitId;

    @TableField("summary")
    private String summary;

    @TableField("submit_count")
    private Integer submitCount;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("is_late")
    private Integer isLate;

    @TableField("status")
    private String status;

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

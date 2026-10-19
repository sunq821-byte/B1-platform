package com.b1.module.submission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("submission_trial")
public class SubmissionTrial implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("submission_id")
    private Long submissionId;

    @TableField("trial_no")
    private Integer trialNo;

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

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("status")
    private String status;

    @TableField("ai_reference_score")
    private BigDecimal aiReferenceScore;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

package com.b1.module.submission.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionVO {

    private Long submissionId;

    private Long trialId;

    private Long taskId;

    private String submissionType;

    private String status;

    private Integer trialNo;

    private Integer aiAnalyzeUsedCount;

    private Integer maxAiAnalyzeCount;

    /** @deprecated 兼容旧前端，等同 trialNo */
    private Integer submitCount;

    /** @deprecated 兼容旧前端，等同 maxAiAnalyzeCount */
    private Integer maxSubmitCount;

    private LocalDateTime submittedAt;

    private LocalDateTime confirmedAt;

    public Integer getSubmitCount() {
        return trialNo != null ? trialNo : submitCount;
    }

    public Integer getMaxSubmitCount() {
        return maxAiAnalyzeCount != null ? maxAiAnalyzeCount : maxSubmitCount;
    }
}

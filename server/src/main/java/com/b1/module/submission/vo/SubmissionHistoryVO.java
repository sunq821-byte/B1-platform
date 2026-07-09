package com.b1.module.submission.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubmissionHistoryVO {

    private Long submissionId;

    private String status;

    private Integer submitCount;

    private LocalDateTime submittedAt;

    private BigDecimal aiScore;
}

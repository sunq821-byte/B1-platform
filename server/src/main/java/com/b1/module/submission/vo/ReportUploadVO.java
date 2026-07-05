package com.b1.module.submission.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportUploadVO {

    private Long reportId;

    private Long fileId;

    private String fileName;

    private Long fileSize;

    private String fileType;

    private LocalDateTime uploadedAt;
}
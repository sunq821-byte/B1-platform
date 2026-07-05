package com.b1.module.file.vo;

import lombok.Data;

@Data
public class FileUploadVO {

    private Long fileId;

    private String url;

    private String originalName;

    private Long fileSize;
}

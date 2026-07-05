package com.b1.module.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_storage")
public class FileStorage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String bucket;

    private String objectKey;

    private String originalName;

    private String contentType;

    private Long fileSize;

    private String fileMd5;

    private String accessUrl;

    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}

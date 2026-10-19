package com.b1.module.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class KnowledgeDocumentUpdateDTO {

    @NotBlank(message = "资料名称不能为空")
    private String title;

    @NotBlank(message = "标签不能为空")
    private String tag;

    @NotBlank(message = "可见范围不能为空")
    private String visibilityScope;

    private List<Long> visibilityClassIds;

    /** 替换文件时传入 */
    private Long fileStorageId;
}

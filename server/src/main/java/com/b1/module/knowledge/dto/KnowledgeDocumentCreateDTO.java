package com.b1.module.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class KnowledgeDocumentCreateDTO {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "文件不能为空")
    private Long fileStorageId;

    @NotBlank(message = "资料名称不能为空")
    private String title;

    @NotBlank(message = "标签不能为空")
    private String tag;

    /** COURSE=课程全体可见；CLASS=指定班级 */
    @NotBlank(message = "可见范围不能为空")
    private String visibilityScope;

    /** scope=CLASS 时必填 */
    private List<Long> visibilityClassIds;
}

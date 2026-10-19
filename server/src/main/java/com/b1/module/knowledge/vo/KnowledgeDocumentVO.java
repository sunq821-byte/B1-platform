package com.b1.module.knowledge.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class KnowledgeDocumentVO {

    private Long id;

    private Long courseId;

    private String courseName;

    private Long fileStorageId;

    private String title;

    private String fileName;

    private String tag;

    private String tagLabel;

    private String tagColor;

    private String visibilityScope;

    private List<String> visibilityLabels;

    private List<Long> visibilityClassIds;

    private Long fileSize;

    private String fileSizeDisplay;

    private String fileType;

    private String downloadUrl;

    private String previewUrl;

    private Integer downloadCount;

    private LocalDateTime uploadedAt;
}

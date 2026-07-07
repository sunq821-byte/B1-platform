package com.b1.module.teacher.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StandardListVO {

    private Long standardId;

    private String standardName;

    private String description;

    private String courseType;

    private String status;

    private Integer isTemplate;

    private Integer dimensionCount;

    private LocalDateTime createTime;
}
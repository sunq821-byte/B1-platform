package com.b1.module.teacher.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {

    private Long courseId;

    private String courseName;

    private String courseCode;

    private String semester;

    private BigDecimal credits;

    private Integer studentCount;

    private Integer taskCount;

    private String status;

    private LocalDateTime createTime;
}
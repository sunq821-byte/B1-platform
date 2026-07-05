package com.b1.module.course.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseVO {

    private Long courseId;

    private String courseName;

    private String courseCode;

    private String teacherName;

    private String semester;

    private BigDecimal credits;

    private Integer taskCount;

    private String description;
}
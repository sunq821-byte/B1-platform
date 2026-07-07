package com.b1.module.teacher.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseUpdateDTO {

    private String courseName;

    private String courseCode;

    private String semester;

    private BigDecimal credits;

    private String description;

    private String syllabus;

    private String objectives;
}
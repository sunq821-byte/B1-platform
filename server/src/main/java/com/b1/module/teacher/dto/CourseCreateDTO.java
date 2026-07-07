package com.b1.module.teacher.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseCreateDTO {

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    @NotBlank(message = "学期不能为空")
    private String semester;

    private BigDecimal credits;

    private String description;

    private String syllabus;

    private String objectives;
}
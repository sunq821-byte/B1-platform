package com.b1.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminClassFormDTO {

    @NotBlank
    private String name;
    private String teacherName;
}

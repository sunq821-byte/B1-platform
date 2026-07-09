package com.b1.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserFormDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String role;
    private String className;
    private String email;
    @NotBlank
    private String status;
}

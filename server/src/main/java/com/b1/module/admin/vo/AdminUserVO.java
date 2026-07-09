package com.b1.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO {

    private String userId;
    private String studentId;
    private String name;
    private String role;
    private String className;
    private String email;
    private String status;
    private String createdAt;
}

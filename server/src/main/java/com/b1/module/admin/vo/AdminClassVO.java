package com.b1.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminClassVO {

    private String id;
    private String name;
    private int studentCount;
    private String teacherName;
    private String createdAt;
}

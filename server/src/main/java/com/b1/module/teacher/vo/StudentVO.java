package com.b1.module.teacher.vo;

import lombok.Data;

@Data
public class StudentVO {

    private Long userId;

    private String realName;

    private String email;

    private String phone;

    private Integer submissionCount;
}
package com.b1.module.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatarUrl;

    private List<String> roles;

    private Integer status;

    private LocalDateTime lastLoginTime;
}

package com.b1.module.user.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatarUrl;

    private List<String> roles;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;
}

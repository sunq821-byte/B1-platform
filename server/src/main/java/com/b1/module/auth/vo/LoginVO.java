package com.b1.module.auth.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private String refreshToken;

    private UserInfoVO userInfo;
}

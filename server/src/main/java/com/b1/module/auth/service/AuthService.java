package com.b1.module.auth.service;

import com.b1.module.auth.dto.LoginDTO;
import com.b1.module.auth.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void logout();

    LoginVO refresh(String refreshToken);
}

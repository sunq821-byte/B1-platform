package com.b1.module.auth.controller;

import com.b1.common.result.Result;
import com.b1.module.auth.dto.LoginDTO;
import com.b1.module.auth.service.AuthService;
import com.b1.module.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录、登出、Token 刷新")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.ok(vo);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginVO vo = authService.refresh(refreshToken);
        return Result.ok(vo);
    }
}

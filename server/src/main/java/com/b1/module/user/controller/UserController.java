package com.b1.module.user.controller;

import com.b1.common.result.Result;
import com.b1.module.file.service.FileService;
import com.b1.module.file.vo.FileUploadVO;
import com.b1.module.user.dto.ChangePasswordDTO;
import com.b1.module.user.dto.UpdateProfileDTO;
import com.b1.module.user.service.UserService;
import com.b1.module.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户管理", description = "个人信息查询修改、密码修改、头像上传")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile() {
        return Result.ok(userService.getProfile());
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(dto));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(dto);
        return Result.ok();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        FileUploadVO upload = fileService.upload(file, "avatars");
        userService.updateAvatar(upload.getUrl());
        return Result.ok(upload.getUrl());
    }
}

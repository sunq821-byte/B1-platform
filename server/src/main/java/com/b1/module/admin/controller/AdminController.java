package com.b1.module.admin.controller;

import com.b1.common.result.Result;
import com.b1.module.admin.dto.AdminClassFormDTO;
import com.b1.module.admin.dto.AdminUserFormDTO;
import com.b1.module.admin.service.AdminService;
import com.b1.module.admin.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员-全部")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> getDashboard() {
        return Result.ok(adminService.getDashboard());
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<List<AdminUserVO>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.listUsers(role, keyword));
    }

    @Operation(summary = "创建用户")
    @PostMapping("/users")
    public Result<AdminUserVO> createUser(@Valid @RequestBody AdminUserFormDTO dto) {
        return Result.ok(adminService.createUser(dto));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/users/{userId}")
    public Result<Void> updateUser(@PathVariable Long userId, @Valid @RequestBody AdminUserFormDTO dto) {
        adminService.updateUser(userId, dto);
        return Result.ok();
    }

    @Operation(summary = "切换用户状态")
    @PatchMapping("/users/{userId}/toggle-status")
    public Result<AdminUserVO> toggleUserStatus(@PathVariable Long userId) {
        return Result.ok(adminService.toggleUserStatus(userId));
    }

    @Operation(summary = "获取班级列表")
    @GetMapping("/classes")
    public Result<List<AdminClassVO>> listClasses() {
        return Result.ok(adminService.listClasses());
    }

    @Operation(summary = "创建班级")
    @PostMapping("/classes")
    public Result<AdminClassVO> createClass(@Valid @RequestBody AdminClassFormDTO dto) {
        return Result.ok(adminService.createClass(dto));
    }

    @Operation(summary = "更新班级")
    @PutMapping("/classes/{classId}")
    public Result<Void> updateClass(@PathVariable Long classId, @Valid @RequestBody AdminClassFormDTO dto) {
        adminService.updateClass(classId, dto);
        return Result.ok();
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/classes/{classId}")
    public Result<Void> deleteClass(@PathVariable Long classId) {
        adminService.deleteClass(classId);
        return Result.ok();
    }

    @Operation(summary = "获取系统配置")
    @GetMapping("/system-config")
    public Result<AdminSystemConfigVO> getSystemConfig() {
        return Result.ok(adminService.getSystemConfig());
    }

    @Operation(summary = "保存系统配置")
    @PutMapping("/system-config")
    public Result<Void> saveSystemConfig(@RequestBody AdminSystemConfigVO config) {
        adminService.saveSystemConfig(config);
        return Result.ok();
    }

    @Operation(summary = "获取操作日志")
    @GetMapping("/logs")
    public Result<List<AdminLogVO>> getLogs(@RequestParam(required = false) String type) {
        return Result.ok(adminService.getLogs(type));
    }

    @Operation(summary = "获取监控数据")
    @GetMapping("/monitor")
    public Result<AdminMonitorVO> getMonitor() {
        return Result.ok(adminService.getMonitor());
    }
}

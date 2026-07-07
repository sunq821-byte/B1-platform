package com.b1.module.student.controller;

import com.b1.common.result.Result;
import com.b1.common.result.PageResult;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生-通知")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentNotificationController {

    private final StudentService studentService;

    @Operation(summary = "通知列表")
    @GetMapping("/notifications")
    public Result<PageResult<NotificationVO>> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(studentService.listNotifications(page, pageSize));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/notifications/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        studentService.markNotificationRead(id);
        return Result.ok();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/notifications/read-all")
    public Result<Void> markAllRead() {
        studentService.markAllNotificationsRead();
        return Result.ok();
    }
}

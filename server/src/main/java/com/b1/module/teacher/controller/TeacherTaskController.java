package com.b1.module.teacher.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.teacher.dto.TaskCreateDTO;
import com.b1.module.teacher.dto.TaskUpdateDTO;
import com.b1.module.teacher.service.TeacherTaskService;
import com.b1.module.teacher.vo.TaskDetailVO;
import com.b1.module.teacher.vo.TaskListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师-任务管理")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherTaskController {

    private final TeacherTaskService teacherTaskService;

    @Operation(summary = "课程任务列表")
    @GetMapping("/courses/{courseId}/tasks")
    public Result<PageResult<TaskListVO>> listTasks(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.ok(teacherTaskService.listTasks(courseId, page, pageSize, keyword, status));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/tasks/{taskId}")
    public Result<TaskDetailVO> getTaskDetail(@PathVariable Long taskId) {
        return Result.ok(teacherTaskService.getTaskDetail(taskId));
    }

    @Operation(summary = "创建任务")
    @PostMapping("/courses/{courseId}/tasks")
    public Result<TaskListVO> createTask(
            @PathVariable Long courseId,
            @Valid @RequestBody TaskCreateDTO dto) {
        return Result.ok(teacherTaskService.createTask(courseId, dto));
    }

    @Operation(summary = "更新任务")
    @PutMapping("/tasks/{taskId}")
    public Result<TaskListVO> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateDTO dto) {
        return Result.ok(teacherTaskService.updateTask(taskId, dto));
    }

    @Operation(summary = "发布任务")
    @PutMapping("/tasks/{taskId}/publish")
    public Result<Void> publishTask(@PathVariable Long taskId) {
        teacherTaskService.publishTask(taskId);
        return Result.ok();
    }
}

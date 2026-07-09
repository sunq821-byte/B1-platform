package com.b1.module.task.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.task.service.TaskService;
import com.b1.module.task.vo.TaskDetailVO;
import com.b1.module.task.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生-任务")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentTaskController {

    private final TaskService taskService;

    @Operation(summary = "学生任务列表")
    @GetMapping("/tasks")
    public PageResult<TaskVO> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return taskService.listTasks(page, pageSize, status, keyword);
    }

    @Operation(summary = "任务详情")
    @GetMapping("/tasks/{taskId}")
    public Result<TaskDetailVO> getTaskDetail(@PathVariable Long taskId) {
        return Result.ok(taskService.getTaskDetail(taskId));
    }
}
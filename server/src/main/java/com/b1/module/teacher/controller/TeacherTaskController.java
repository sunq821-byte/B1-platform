package com.b1.module.teacher.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseTeacherMapper;
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

import java.util.ArrayList;
import java.util.List;

@Tag(name = "教师-任务管理")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherTaskController {

    private final TeacherTaskService teacherTaskService;
    private final CourseTeacherMapper courseTeacherMapper;

    @Operation(summary = "课程任务列表")
    @GetMapping("/courses/{courseId}/tasks")
    public PageResult<TaskListVO> listTasks(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return teacherTaskService.listTasks(courseId, page, pageSize, keyword, status);
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
    @PostMapping("/tasks/{taskId}/publish")
    public Result<Void> publishTask(@PathVariable Long taskId) {
        teacherTaskService.publishTask(taskId);
        return Result.ok();
    }

    @Operation(summary = "任务列表(跨课程)")
    @GetMapping("/tasks")
    public PageResult<TaskListVO> listAllTasks(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return teacherTaskService.listAllTasks(courseId, page, pageSize, keyword, status);
    }

    @Operation(summary = "创建任务(跨课程)")
    @PostMapping("/tasks")
    public Result<TaskListVO> createTaskDirect(@Valid @RequestBody TaskCreateDTO dto) {
        Long courseId = dto.getCourseId();
        if (courseId == null) {
            throw new com.b1.common.exception.BusinessException(
                    com.b1.common.exception.ErrorCode.PARAM_ERROR, "课程ID不能为空");
        }
        return Result.ok(teacherTaskService.createTask(courseId, dto));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/tasks/{taskId}")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        teacherTaskService.deleteTask(taskId);
        return Result.ok();
    }
}

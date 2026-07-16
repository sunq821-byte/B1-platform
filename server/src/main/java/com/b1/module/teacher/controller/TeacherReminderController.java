package com.b1.module.teacher.controller;

import com.b1.common.result.Result;
import com.b1.module.teacher.dto.RemindRequestDTO;
import com.b1.module.teacher.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "教师-催交")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherReminderController {

    private final TeacherService teacherService;

    @Operation(summary = "批量催交（向未提交学生发送站内通知）")
    @PostMapping("/tasks/remind")
    public Result<Map<String, Object>> remind(@Valid @RequestBody RemindRequestDTO dto) {
        int notified = teacherService.remindUnsubmitted(dto.getTaskIds());
        return Result.ok(Map.of(
                "notifiedStudents", notified,
                "taskCount", dto.getTaskIds().size()));
    }
}

package com.b1.module.teacher.controller;

import com.b1.common.result.Result;
import com.b1.module.teacher.service.TeacherService;
import com.b1.module.teacher.vo.TeacherDashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师-仪表盘")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherDashboardController {

    private final TeacherService teacherService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<TeacherDashboardVO> getDashboard() {
        return Result.ok(teacherService.getDashboard());
    }
}

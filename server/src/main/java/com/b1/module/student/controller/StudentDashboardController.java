package com.b1.module.student.controller;

import com.b1.common.result.Result;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.DashboardVO;
import com.b1.module.student.vo.StudentReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生-仪表盘")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentService studentService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.ok(studentService.getDashboard());
    }

    @Operation(summary = "获取学生报告")
    @GetMapping("/student-report")
    public Result<StudentReportVO> getStudentReport() {
        return Result.ok(studentService.getStudentReport());
    }
}

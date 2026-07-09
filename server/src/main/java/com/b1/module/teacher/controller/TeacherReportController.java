package com.b1.module.teacher.controller;

import com.b1.common.result.Result;
import com.b1.module.export.service.ReportExportService;
import com.b1.module.teacher.service.TeacherReportService;
import com.b1.module.teacher.vo.ClassReportVO;
import com.b1.module.teacher.vo.CollegeReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师报告", description = "班级报告、学院报告")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherReportController {

    private final TeacherReportService reportService;
    private final ReportExportService reportExportService;

    @Operation(summary = "班级报告")
    @GetMapping("/reports")
    public Result<ClassReportVO> getClassReport(@RequestParam(required = false) String className) {
        return Result.ok(reportService.getClassReport(className));
    }

    @Operation(summary = "学院报告")
    @GetMapping("/reports-college")
    public Result<CollegeReportVO> getCollegeReport() {
        return Result.ok(reportService.getCollegeReport());
    }

    @Operation(summary = "导出班级报告", description = "format: xlsx 或 pdf")
    @GetMapping("/reports/export")
    public void exportClassReport(@RequestParam(required = false) String className,
                                  @RequestParam(defaultValue = "xlsx") String format,
                                  HttpServletResponse response) {
        reportExportService.exportClassReport(className, format, response);
    }
}

package com.b1.module.student.controller;

import com.b1.common.result.Result;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.GrowthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生-成长档案")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentGrowthController {

    private final StudentService studentService;

    @Operation(summary = "获取成长档案")
    @GetMapping("/growth-profile")
    public Result<GrowthProfileVO> getGrowthProfile() {
        return Result.ok(studentService.getGrowthProfile());
    }
}

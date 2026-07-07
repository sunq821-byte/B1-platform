package com.b1.module.teacher.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.teacher.dto.StandardCreateDTO;
import com.b1.module.teacher.dto.StandardUpdateDTO;
import com.b1.module.teacher.service.TeacherService;
import com.b1.module.teacher.vo.StandardDetailVO;
import com.b1.module.teacher.vo.StandardListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师-评价标准")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherStandardController {

    private final TeacherService teacherService;

    @Operation(summary = "评价标准列表")
    @GetMapping("/standards")
    public Result<PageResult<StandardListVO>> listStandards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(teacherService.listStandards(page, pageSize, keyword));
    }

    @Operation(summary = "评价标准详情")
    @GetMapping("/standards/{standardId}")
    public Result<StandardDetailVO> getStandardDetail(@PathVariable Long standardId) {
        return Result.ok(teacherService.getStandardDetail(standardId));
    }

    @Operation(summary = "创建评价标准")
    @PostMapping("/standards")
    public Result<StandardListVO> createStandard(@Valid @RequestBody StandardCreateDTO dto) {
        return Result.ok(teacherService.createStandard(dto));
    }

    @Operation(summary = "更新评价标准")
    @PutMapping("/standards/{standardId}")
    public Result<StandardListVO> updateStandard(
            @PathVariable Long standardId,
            @Valid @RequestBody StandardUpdateDTO dto) {
        return Result.ok(teacherService.updateStandard(standardId, dto));
    }
}

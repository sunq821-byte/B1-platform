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
    public PageResult<StandardListVO> listStandards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return teacherService.listStandards(page, pageSize, keyword, null);
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

    @Operation(summary = "评价标准维度")
    @GetMapping("/standards/{standardId}/dimensions")
    public Result<StandardDetailVO> getStandardDimensions(@PathVariable Long standardId) {
        return Result.ok(teacherService.getStandardDetail(standardId));
    }

    @Operation(summary = "更新评价标准维度")
    @PutMapping("/standards/{standardId}/dimensions")
    public Result<Void> updateStandardDimensions(
            @PathVariable Long standardId,
            @RequestBody java.util.Map<String, Object> body) {
        return Result.ok();
    }

    @Operation(summary = "评价标准库")
    @GetMapping("/standards-library")
    public PageResult<StandardListVO> listStandardTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return teacherService.listStandards(page, pageSize, null, 1);
    }

    @Operation(summary = "复制评价标准模板")
    @PostMapping("/standards/{standardId}/copy")
    public Result<StandardListVO> copyStandard(@PathVariable Long standardId) {
        return Result.ok(teacherService.copyStandard(standardId));
    }
}

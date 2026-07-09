package com.b1.module.teacher.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.teacher.dto.ReviewSubmitDTO;
import com.b1.module.teacher.service.TeacherReviewService;
import com.b1.module.teacher.vo.SubmissionDetailVO;
import com.b1.module.teacher.vo.SubmissionListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师-提交批阅")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherReviewController {

    private final TeacherReviewService teacherReviewService;

    @Operation(summary = "任务提交列表")
    @GetMapping("/tasks/{taskId}/submissions")
    public PageResult<SubmissionListVO> listSubmissions(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return teacherReviewService.listSubmissions(taskId, page, pageSize, status, keyword);
    }

    @Operation(summary = "提交详情")
    @GetMapping("/submissions/{submissionId}")
    public Result<SubmissionDetailVO> getSubmissionDetail(@PathVariable Long submissionId) {
        return Result.ok(teacherReviewService.getSubmissionDetail(submissionId));
    }

    @Operation(summary = "批阅提交")
    @PostMapping("/submissions/{submissionId}/review")
    public Result<Void> reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmitDTO dto) {
        teacherReviewService.reviewSubmission(submissionId, dto);
        return Result.ok();
    }

    @Operation(summary = "查看AI分析结果")
    @GetMapping("/submissions/{submissionId}/ai-result")
    public Result<AiResultVO> getSubmissionAiResult(@PathVariable Long submissionId) {
        return Result.ok(teacherReviewService.getSubmissionAiResult(submissionId));
    }

    @Operation(summary = "提交列表(跨任务)")
    @GetMapping("/submissions")
    public PageResult<SubmissionListVO> listAllSubmissions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return teacherReviewService.listAllSubmissions(page, pageSize, status, keyword);
    }

    @Operation(summary = "AI诊断结果")
    @GetMapping("/submissions/{submissionId}/diagnosis")
    public Result<AiResultVO> getDiagnosis(@PathVariable Long submissionId) {
        return Result.ok(teacherReviewService.getSubmissionAiResult(submissionId));
    }

    @Operation(summary = "发布/打回批阅结果")
    @PostMapping("/submissions/{submissionId}/publish")
    public Result<Void> publishReview(@PathVariable Long submissionId, @Valid @RequestBody ReviewSubmitDTO dto) {
        teacherReviewService.reviewSubmission(submissionId, dto);
        return Result.ok();
    }
}

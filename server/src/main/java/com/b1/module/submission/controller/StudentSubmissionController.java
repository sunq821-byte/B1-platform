package com.b1.module.submission.controller;

import com.b1.common.result.Result;
import com.b1.module.submission.dto.GitVerifyDTO;
import com.b1.module.submission.dto.ConfirmSubmitDTO;
import com.b1.module.submission.dto.SubmitRequestDTO;
import com.b1.module.submission.service.SubmissionService;
import com.b1.module.submission.vo.GitVerifyResultVO;
import com.b1.module.submission.vo.ReportUploadVO;
import com.b1.module.submission.vo.SubmissionDraftVO;
import com.b1.module.submission.vo.SubmissionHistoryVO;
import com.b1.module.submission.vo.SubmissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "学生-提交")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentSubmissionController {

    private final SubmissionService submissionService;

    @Operation(summary = "试交成果")
    @PostMapping("/tasks/{taskId}/trials")
    public Result<SubmissionVO> trialSubmit(
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitRequestDTO dto) {
        return Result.ok(submissionService.trialSubmit(taskId, dto));
    }

    @Operation(summary = "确认提交教师审核")
    @PostMapping("/tasks/{taskId}/confirm")
    public Result<SubmissionVO> confirmSubmit(
            @PathVariable Long taskId,
            @RequestBody(required = false) ConfirmSubmitDTO dto) {
        return Result.ok(submissionService.confirmSubmit(taskId, dto));
    }

    @Operation(summary = "提交成果（兼容旧接口，等同试交）")
    @PostMapping("/tasks/{taskId}/submissions")
    public Result<SubmissionVO> submit(
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitRequestDTO dto) {
        return Result.ok(submissionService.trialSubmit(taskId, dto));
    }

    @Operation(summary = "提交历史")
    @GetMapping("/tasks/{taskId}/submissions")
    public Result<List<SubmissionHistoryVO>> listHistory(@PathVariable Long taskId) {
        return Result.ok(submissionService.listHistory(taskId));
    }

    @Operation(summary = "获取当前试交草稿（在线代码/备注）")
    @GetMapping("/tasks/{taskId}/draft")
    public Result<SubmissionDraftVO> getDraft(@PathVariable Long taskId) {
        return Result.ok(submissionService.getDraft(taskId));
    }

    @Operation(summary = "验证 Git 仓库")
    @PostMapping("/tasks/{taskId}/git-verify")
    public Result<GitVerifyResultVO> verifyGit(
            @PathVariable Long taskId,
            @Valid @RequestBody GitVerifyDTO dto) {
        return Result.ok(submissionService.verifyGit(taskId, dto));
    }

    @Operation(summary = "上传实训报告")
    @PostMapping("/tasks/{taskId}/reports")
    public Result<ReportUploadVO> uploadReport(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title) {
        return Result.ok(submissionService.uploadReport(taskId, file, title));
    }
}
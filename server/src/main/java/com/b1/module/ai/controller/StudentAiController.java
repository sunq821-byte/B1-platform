package com.b1.module.ai.controller;

import com.b1.common.result.Result;
import com.b1.module.ai.service.AiService;
import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.ai.vo.EvaluationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生-AI评价")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentAiController {

    private final AiService aiService;

    @Operation(summary = "发起 AI 分析")
    @PostMapping("/submissions/{submissionId}/ai-evaluate")
    public Result<AiResultVO> initiateEvaluation(@PathVariable Long submissionId) {
        return Result.ok(aiService.initiateEvaluation(submissionId));
    }

    @Operation(summary = "获取 AI 分析结果")
    @GetMapping("/submissions/{submissionId}/ai-result")
    public Result<AiResultVO> getAiResult(@PathVariable Long submissionId) {
        return Result.ok(aiService.getAiResult(submissionId));
    }

    @Operation(summary = "获取完整评价")
    @GetMapping("/submissions/{submissionId}/evaluation")
    public Result<EvaluationVO> getFullEvaluation(@PathVariable Long submissionId) {
        return Result.ok(aiService.getFullEvaluation(submissionId));
    }
}
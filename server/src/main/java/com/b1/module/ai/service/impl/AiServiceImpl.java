package com.b1.module.ai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.ai.entity.AiAnalysis;
import com.b1.module.ai.entity.AiAnalysisDetail;
import com.b1.module.ai.mapper.AiAnalysisDetailMapper;
import com.b1.module.ai.mapper.AiAnalysisMapper;
import com.b1.module.ai.provider.AiProvider;
import com.b1.module.ai.provider.AiProviderRouter;
import com.b1.module.ai.provider.dto.AiRequest;
import com.b1.module.ai.provider.dto.AiResponse;
import com.b1.module.ai.provider.dto.AiResponse.DimensionResult;
import com.b1.module.ai.service.AiService;
import com.b1.module.ai.service.PromptBuilder;
import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.ai.vo.EvaluationVO;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.file.service.FileService;
import com.b1.module.review.entity.TeacherReview;
import com.b1.module.review.mapper.TeacherReviewMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.standard.entity.EvaluationStandard;
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.EvaluationStandardMapper;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.entity.SubmissionFile;
import com.b1.module.submission.mapper.SubmissionFileMapper;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionFileMapper submissionFileMapper;
    private final AiAnalysisMapper aiAnalysisMapper;
    private final AiAnalysisDetailMapper aiAnalysisDetailMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final CourseMapper courseMapper;
    private final TeacherReviewMapper teacherReviewMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final UserMapper userMapper;
    private final StandardDimensionMapper standardDimensionMapper;
    private final EvaluationStandardMapper evaluationStandardMapper;
    private final FileService fileService;
    private final AiProviderRouter providerRouter;
    private final PromptBuilder promptBuilder;
    private final Executor aiExecutor;

    public AiServiceImpl(
            SubmissionMapper submissionMapper,
            SubmissionFileMapper submissionFileMapper,
            AiAnalysisMapper aiAnalysisMapper,
            AiAnalysisDetailMapper aiAnalysisDetailMapper,
            TrainingTaskMapper trainingTaskMapper,
            CourseMapper courseMapper,
            TeacherReviewMapper teacherReviewMapper,
            ScoreRecordMapper scoreRecordMapper,
            UserMapper userMapper,
            StandardDimensionMapper standardDimensionMapper,
            EvaluationStandardMapper evaluationStandardMapper,
            FileService fileService,
            AiProviderRouter providerRouter,
            PromptBuilder promptBuilder,
            @Qualifier("ai-analysis-executor") Executor aiExecutor) {
        this.submissionMapper = submissionMapper;
        this.submissionFileMapper = submissionFileMapper;
        this.aiAnalysisMapper = aiAnalysisMapper;
        this.aiAnalysisDetailMapper = aiAnalysisDetailMapper;
        this.trainingTaskMapper = trainingTaskMapper;
        this.courseMapper = courseMapper;
        this.teacherReviewMapper = teacherReviewMapper;
        this.scoreRecordMapper = scoreRecordMapper;
        this.userMapper = userMapper;
        this.standardDimensionMapper = standardDimensionMapper;
        this.evaluationStandardMapper = evaluationStandardMapper;
        this.fileService = fileService;
        this.providerRouter = providerRouter;
        this.promptBuilder = promptBuilder;
        this.aiExecutor = aiExecutor;
    }

    @Override
    public AiResultVO initiateEvaluation(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提交记录不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!submission.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该提交");
        }

        AiAnalysis existing = aiAnalysisMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysis>()
                        .eq(AiAnalysis::getSubmissionId, submissionId)
                        .orderByDesc(AiAnalysis::getId)
                        .last("LIMIT 1"));

        if (existing != null) {
            if ("PENDING".equals(existing.getAnalysisStatus()) || "PROCESSING".equals(existing.getAnalysisStatus())) {
                return buildProcessingResult(existing);
            }
            // Re-submission: if submission was updated after analysis completed, re-analyze
            if ("COMPLETED".equals(existing.getAnalysisStatus())) {
                if (submission.getSubmitTime() != null
                        && existing.getCompleteTime() != null
                        && submission.getSubmitTime().isAfter(existing.getCompleteTime())) {
                    aiAnalysisMapper.deleteById(existing.getId());
                } else {
                    return buildCompletedResult(existing);
                }
            } else {
                aiAnalysisMapper.deleteById(existing.getId());
            }
        }

        AiAnalysis aiAnalysis = new AiAnalysis();
        aiAnalysis.setSubmissionId(submissionId);
        aiAnalysis.setAnalysisStatus("PROCESSING");
        aiAnalysis.setStartTime(LocalDateTime.now());
        aiAnalysisMapper.insert(aiAnalysis);

        // Run AI analysis in background thread so the HTTP request returns immediately
        aiExecutor.execute(() -> processAnalysis(aiAnalysis, submission));

        return buildProcessingResult(aiAnalysis);
    }

    /**
     * Background AI analysis — called asynchronously so the HTTP trigger endpoint
     * returns immediately with PROCESSING status. The frontend polls getAiResult()
     * until COMPLETED or FAILED.
     */
    private void processAnalysis(AiAnalysis aiAnalysis, Submission submission) {
        try {
            TrainingTask task = trainingTaskMapper.selectById(submission.getTrainingTaskId());
            String taskDescription = task != null && task.getDescription() != null ? task.getDescription() : "";

            List<StandardDimension> dimensions = getDimensions(task);
            String systemPrompt = promptBuilder.buildSystemPrompt(dimensions);

            List<SubmissionFile> files = submissionFileMapper.selectList(
                    new LambdaQueryWrapper<SubmissionFile>()
                            .eq(SubmissionFile::getSubmissionId, submission.getId())
                            .orderByAsc(SubmissionFile::getSortOrder));

            if (files.isEmpty()) {
                aiAnalysis.setAnalysisStatus("FAILED");
                aiAnalysis.setErrorMessage("没有找到提交文件");
                aiAnalysis.setCompleteTime(LocalDateTime.now());
                aiAnalysisMapper.updateById(aiAnalysis);
                return;
            }

            List<AiAnalysisDetail> allDetails = new ArrayList<>();
            BigDecimal totalWeightedScore = BigDecimal.ZERO;
            BigDecimal totalWeight = BigDecimal.ZERO;
            int totalInputTokens = 0;
            int totalOutputTokens = 0;
            StringBuilder combinedRawResponse = new StringBuilder("{\"responses\":[");
            boolean firstResp = true;

            for (SubmissionFile file : files) {
                AiProvider provider = providerRouter.route(file);
                String fileName = file.getFileName();

                log.info("Analyzing file {} with provider {}", fileName, provider.getProviderName());

                AiRequest request;
                if (provider.getProviderName().equals("QWEN")) {
                    String imageUrl = fileService.getAccessUrl(file.getFileId());
                    String userPrompt = promptBuilder.buildScreenshotReviewPrompt(taskDescription);
                    request = AiRequest.vision(systemPrompt, userPrompt, List.of(imageUrl));
                } else {
                    String codeContent = readFileContent(file);
                    String userPrompt = promptBuilder.buildCodeReviewPrompt(codeContent, fileName, taskDescription);
                    request = AiRequest.textOnly(systemPrompt, userPrompt);
                }

                AiResponse response = provider.analyze(request);

                if (response.isSuccess()) {
                    for (DimensionResult dr : response.getDimensions()) {
                        AiAnalysisDetail detail = new AiAnalysisDetail();
                        detail.setAiAnalysisId(aiAnalysis.getId());
                        detail.setAgentType(provider.getProviderName());
                        detail.setFilePath(fileName);
                        detail.setLineNumber(dr.getLineNumber());
                        detail.setIssueType(dr.getIssueType());
                        detail.setSeverity(dr.getSeverity());
                        detail.setReason(dr.getReason());
                        detail.setSuggestion(dr.getSuggestion());
                        detail.setSuggestDeduct(dr.getSuggestDeduct());
                        detail.setConfidence(dr.getConfidence());
                        detail.setSortOrder(allDetails.size() + 1);

                        StandardDimension matchedDim = findMatchingDimension(dimensions, dr);
                        if (matchedDim != null) {
                            detail.setDimensionId(matchedDim.getId());
                        }

                        allDetails.add(detail);
                    }

                    if (response.getOverallScore() != null) {
                        BigDecimal weight = BigDecimal.ONE;
                        totalWeightedScore = totalWeightedScore.add(response.getOverallScore().multiply(weight));
                        totalWeight = totalWeight.add(weight);
                    }

                    totalInputTokens += response.getTokenInput() != null ? response.getTokenInput() : 0;
                    totalOutputTokens += response.getTokenOutput() != null ? response.getTokenOutput() : 0;

                    if (response.getRawContent() != null) {
                        if (!firstResp) combinedRawResponse.append(",");
                        firstResp = false;
                        String escaped = response.getRawContent()
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                        combinedRawResponse.append("{\"provider\":\"")
                                .append(provider.getProviderName())
                                .append("\",\"file\":\"")
                                .append(fileName.replace("\\", "\\\\").replace("\"", "\\\""))
                                .append("\",\"content\":\"")
                                .append(escaped)
                                .append("\"}");
                    }
                } else {
                    log.warn("Provider {} failed for file {}: {}", provider.getProviderName(), fileName, response.getRawContent());
                }
            }

            for (AiAnalysisDetail detail : allDetails) {
                aiAnalysisDetailMapper.insert(detail);
            }

            BigDecimal overallScore = BigDecimal.ZERO;
            if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                overallScore = totalWeightedScore.divide(totalWeight, 0, RoundingMode.HALF_UP);
            }

            aiAnalysis.setTotalScore(overallScore);
            aiAnalysis.setAnalysisStatus("COMPLETED");
            combinedRawResponse.append("]}");
            aiAnalysis.setRawResponse(combinedRawResponse.toString());
            aiAnalysis.setModelProvider("DEEPSEEK+QWEN");
            aiAnalysis.setModelName("deepseek-chat+qwen-vl-max");
            aiAnalysis.setTokenInput(totalInputTokens);
            aiAnalysis.setTokenOutput(totalOutputTokens);
            aiAnalysis.setTokenTotal(totalInputTokens + totalOutputTokens);
            aiAnalysis.setCompleteTime(LocalDateTime.now());
            aiAnalysis.setAnalysisTimeMs((int) (System.currentTimeMillis() - aiAnalysis.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()));
            aiAnalysisMapper.updateById(aiAnalysis);

        } catch (Exception e) {
            log.error("AI analysis failed for submission {}", submission.getId(), e);
            aiAnalysis.setAnalysisStatus("FAILED");
            aiAnalysis.setErrorMessage(e.getMessage());
            aiAnalysis.setCompleteTime(LocalDateTime.now());
            aiAnalysisMapper.updateById(aiAnalysis);
        }
    }

    @Override
    public AiResultVO getAiResult(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提交记录不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!submission.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该提交");
        }

        AiAnalysis aiAnalysis = aiAnalysisMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysis>()
                        .eq(AiAnalysis::getSubmissionId, submissionId)
                        .orderByDesc(AiAnalysis::getId)
                        .last("LIMIT 1"));

        if (aiAnalysis == null) {
            AiResultVO vo = new AiResultVO();
            vo.setStatus("NOT_STARTED");
            vo.setProgress(0);
            return vo;
        }

        return switch (aiAnalysis.getAnalysisStatus()) {
            case "PENDING" -> buildPendingResult(aiAnalysis);
            case "PROCESSING" -> buildProcessingResult(aiAnalysis);
            case "COMPLETED" -> buildCompletedResult(aiAnalysis);
            case "FAILED" -> buildFailedResult(aiAnalysis);
            default -> buildPendingResult(aiAnalysis);
        };
    }

    @Override
    public EvaluationVO getFullEvaluation(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提交记录不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!submission.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该提交");
        }

        EvaluationVO vo = new EvaluationVO();
        vo.setSubmissionId(submissionId);
        vo.setTaskId(submission.getTrainingTaskId());
        vo.setSubmittedAt(submission.getSubmitTime());

        TrainingTask task = trainingTaskMapper.selectById(submission.getTrainingTaskId());
        if (task != null) {
            vo.setTaskName(task.getTaskName());

            Course course = courseMapper.selectById(task.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }

        AiAnalysis aiAnalysis = aiAnalysisMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysis>()
                        .eq(AiAnalysis::getSubmissionId, submissionId)
                        .orderByDesc(AiAnalysis::getId)
                        .last("LIMIT 1"));

        if (aiAnalysis != null) {
            EvaluationVO.AiEvalSummaryVO aiEval = new EvaluationVO.AiEvalSummaryVO();
            aiEval.setOverallScore(aiAnalysis.getTotalScore());
            aiEval.setSummary(aiAnalysis.getRawResponse());
            aiEval.setCompletedAt(aiAnalysis.getCompleteTime());
            vo.setAiEvaluation(aiEval);
        }

        TeacherReview review = teacherReviewMapper.selectOne(
                new LambdaQueryWrapper<TeacherReview>()
                        .eq(TeacherReview::getSubmissionId, submissionId)
                        .orderByDesc(TeacherReview::getId)
                        .last("LIMIT 1"));

        if (review != null) {
            EvaluationVO.TeacherEvalSummaryVO teacherEval = new EvaluationVO.TeacherEvalSummaryVO();
            teacherEval.setComment(review.getTeacherComment());
            teacherEval.setScoredAt(review.getReviewTime());
            teacherEval.setPublishedAt(review.getPublishTime());

            User reviewer = userMapper.selectById(review.getReviewerId());
            if (reviewer != null) {
                teacherEval.setScoredBy(reviewer.getRealName());
            }

            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>()
                            .eq(ScoreRecord::getSubmissionId, submissionId));
            if (!scores.isEmpty()) {
                ScoreRecord scoreRecord = scores.get(0);
                teacherEval.setOverallScore(scoreRecord.getTotalScore());
                vo.setFinalScore(scoreRecord.getTotalScore());
            }

            teacherEval.setDimensions(Collections.emptyList());
            vo.setTeacherEvaluation(teacherEval);

            if ("REJECTED".equals(review.getStatus())) {
                vo.setStatus("REJECTED");
                vo.setRejectReason(review.getTeacherComment());
            } else {
                vo.setStatus("COMPLETED");
            }
        } else {
            vo.setStatus(submission.getStatus() != null ? submission.getStatus() : "SUBMITTED");
        }

        return vo;
    }

    private List<StandardDimension> getDimensions(TrainingTask task) {
        if (task == null || task.getStandardId() == null) {
            return Collections.emptyList();
        }
        return standardDimensionMapper.selectList(
                new LambdaQueryWrapper<StandardDimension>()
                        .eq(StandardDimension::getStandardId, task.getStandardId())
                        .orderByAsc(StandardDimension::getSortOrder));
    }

    private StandardDimension findMatchingDimension(List<StandardDimension> dimensions, DimensionResult dr) {
        return dimensions.stream()
                .filter(d -> d.getDimName().equals(dr.getDimensionName())
                        || d.getDimName().contains(dr.getDimensionName())
                        || (dr.getDimensionName() != null && dr.getDimensionName().contains(d.getDimName())))
                .findFirst()
                .orElse(null);
    }

    private String readFileContent(SubmissionFile file) {
        try {
            InputStream is = fileService.download(file.getFileId());
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Cannot read file content for fileId={}, fileName={}", file.getFileId(), file.getFileName());
            return "[文件无法读取: " + file.getFileName() + "]";
        }
    }

    private AiResultVO buildPendingResult(AiAnalysis aiAnalysis) {
        AiResultVO vo = new AiResultVO();
        vo.setAnalyzeId(aiAnalysis.getId());
        vo.setStatus("PENDING");
        vo.setProgress(0);
        vo.setStartedAt(aiAnalysis.getStartTime());
        return vo;
    }

    private AiResultVO buildProcessingResult(AiAnalysis aiAnalysis) {
        AiResultVO vo = new AiResultVO();
        vo.setAnalyzeId(aiAnalysis.getId());
        vo.setStatus("PROCESSING");
        vo.setProgress(50);
        vo.setCurrentDimension("代码规范");
        vo.setStartedAt(aiAnalysis.getStartTime());
        return vo;
    }

    private AiResultVO buildCompletedResult(AiAnalysis aiAnalysis) {
        AiResultVO vo = new AiResultVO();
        vo.setAnalyzeId(aiAnalysis.getId());
        vo.setStatus("COMPLETED");
        vo.setProgress(100);
        vo.setStartedAt(aiAnalysis.getStartTime());
        vo.setCompletedAt(aiAnalysis.getCompleteTime());

        List<AiAnalysisDetail> details = aiAnalysisDetailMapper.selectList(
                new LambdaQueryWrapper<AiAnalysisDetail>()
                        .eq(AiAnalysisDetail::getAiAnalysisId, aiAnalysis.getId())
                        .orderByAsc(AiAnalysisDetail::getSortOrder));

        List<AiResultVO.AiDimensionScoreVO> dimVOs = new ArrayList<>();
        for (AiAnalysisDetail detail : details) {
            AiResultVO.AiDimensionScoreVO dimVO = new AiResultVO.AiDimensionScoreVO();
            dimVO.setScore(detail.getSuggestDeduct() != null
                    ? BigDecimal.ZERO.max(new BigDecimal("100").subtract(detail.getSuggestDeduct()))
                    : BigDecimal.ZERO);
            dimVO.setComment(detail.getReason());
            dimVO.setSuggestions(detail.getSuggestion() != null
                    ? List.of(detail.getSuggestion()) : Collections.emptyList());
            dimVO.setCodeReferences(Collections.emptyList());

            if (detail.getDimensionId() != null) {
                StandardDimension dim = standardDimensionMapper.selectById(detail.getDimensionId());
                if (dim != null) {
                    dimVO.setDimensionName(dim.getDimName());
                    dimVO.setMaxScore(dim.getMaxScore());
                    dimVO.setWeight(dim.getWeight());
                }
            }

            dimVOs.add(dimVO);
        }

        AiResultVO.AiScoreResultVO result = new AiResultVO.AiScoreResultVO();
        result.setOverallScore(aiAnalysis.getTotalScore());
        result.setDimensions(dimVOs);
        result.setSummary(String.format("AI分析完成，总分%d分，共发现%d个问题。",
                aiAnalysis.getTotalScore() != null ? aiAnalysis.getTotalScore().intValue() : 0,
                dimVOs.size()));
        result.setStrengths(Collections.emptyList());
        result.setWeaknesses(Collections.emptyList());
        result.setImprovementPlan("");

        vo.setResult(result);
        return vo;
    }

    private AiResultVO buildFailedResult(AiAnalysis aiAnalysis) {
        AiResultVO vo = new AiResultVO();
        vo.setAnalyzeId(aiAnalysis.getId());
        vo.setStatus("FAILED");
        vo.setProgress(0);
        vo.setStartedAt(aiAnalysis.getStartTime());
        vo.setCompletedAt(aiAnalysis.getCompleteTime());
        return vo;
    }
}

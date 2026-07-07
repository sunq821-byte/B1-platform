package com.b1.module.ai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.ai.entity.AiAnalysis;
import com.b1.module.ai.entity.AiAnalysisDetail;
import com.b1.module.ai.mapper.AiAnalysisDetailMapper;
import com.b1.module.ai.mapper.AiAnalysisMapper;
import com.b1.module.ai.service.AiService;
import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.ai.vo.EvaluationVO;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.review.entity.TeacherReview;
import com.b1.module.review.mapper.TeacherReviewMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final SubmissionMapper submissionMapper;
    private final AiAnalysisMapper aiAnalysisMapper;
    private final AiAnalysisDetailMapper aiAnalysisDetailMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final CourseMapper courseMapper;
    private final TeacherReviewMapper teacherReviewMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final UserMapper userMapper;
    private final StandardDimensionMapper standardDimensionMapper;

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
            if ("COMPLETED".equals(existing.getAnalysisStatus())) {
                return buildCompletedResult(existing);
            }
            if ("PENDING".equals(existing.getAnalysisStatus()) || "PROCESSING".equals(existing.getAnalysisStatus())) {
                return buildProcessingResult(existing);
            }
        }

        AiAnalysis aiAnalysis = new AiAnalysis();
        aiAnalysis.setSubmissionId(submissionId);
        aiAnalysis.setAnalysisStatus("PENDING");
        aiAnalysis.setStartTime(LocalDateTime.now());
        aiAnalysisMapper.insert(aiAnalysis);

        AiResultVO vo = new AiResultVO();
        vo.setAnalyzeId(aiAnalysis.getId());
        vo.setStatus("PENDING");
        vo.setProgress(0);
        vo.setStartedAt(aiAnalysis.getStartTime());
        return vo;
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
            } else {
                vo.setStatus("COMPLETED");
            }
        } else {
            vo.setStatus(submission.getStatus() != null ? submission.getStatus() : "SUBMITTED");
        }

        return vo;
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
        result.setSummary(aiAnalysis.getRawResponse());
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
package com.b1.module.teacher.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.common.result.PageResult;
import com.b1.module.ai.entity.AiAnalysis;
import com.b1.module.ai.entity.AiAnalysisDetail;
import com.b1.module.ai.mapper.AiAnalysisDetailMapper;
import com.b1.module.ai.mapper.AiAnalysisMapper;
import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.file.entity.FileStorage;
import com.b1.module.file.mapper.FileStorageMapper;
import com.b1.module.review.entity.TeacherReview;
import com.b1.module.review.mapper.TeacherReviewMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.entity.SubmissionFile;
import com.b1.module.submission.mapper.SubmissionFileMapper;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.dto.ReviewSubmitDTO;
import com.b1.module.teacher.service.TeacherReviewService;
import com.b1.module.teacher.vo.SubmissionDetailVO;
import com.b1.module.teacher.vo.SubmissionListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherReviewServiceImpl implements TeacherReviewService {

    private final TrainingTaskMapper trainingTaskMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionFileMapper submissionFileMapper;
    private final FileStorageMapper fileStorageMapper;
    private final UserMapper userMapper;
    private final TeacherReviewMapper teacherReviewMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final StandardDimensionMapper standardDimensionMapper;
    private final AiAnalysisMapper aiAnalysisMapper;
    private final AiAnalysisDetailMapper aiAnalysisDetailMapper;

    @Override
    public PageResult<SubmissionListVO> listSubmissions(Long taskId, int page, int pageSize, String status, String keyword) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该任务");
        }

        LambdaQueryWrapper<Submission> query = new LambdaQueryWrapper<Submission>()
                .eq(Submission::getTrainingTaskId, taskId)
                .eq(Submission::getDeleted, 0);
        if (StringUtils.hasText(status)) {
            query.eq(Submission::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            List<User> matchedUsers = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getRealName, keyword));
            if (!matchedUsers.isEmpty()) {
                List<Long> userIds = matchedUsers.stream().map(User::getId).toList();
                query.in(Submission::getUserId, userIds);
            } else {
                return PageResult.of(Collections.emptyList(), page, pageSize, 0);
            }
        }
        query.orderByDesc(Submission::getSubmitTime);

        IPage<Submission> submissionPage = submissionMapper.selectPage(
                new Page<>(page, pageSize), query);

        List<Submission> submissions = submissionPage.getRecords();
        List<Long> userIds = submissions.stream().map(Submission::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
        Set<Long> reviewedSubmissionIds = Collections.emptySet();
        if (!submissionIds.isEmpty()) {
            reviewedSubmissionIds = teacherReviewMapper.selectList(
                    new LambdaQueryWrapper<TeacherReview>()
                            .in(TeacherReview::getSubmissionId, submissionIds))
                    .stream().map(TeacherReview::getSubmissionId).collect(Collectors.toSet());
        }

        List<SubmissionListVO> vos = new ArrayList<>();
        for (Submission s : submissions) {
            User u = userMap.get(s.getUserId());
            SubmissionListVO vo = new SubmissionListVO();
            vo.setSubmissionId(s.getId());
            vo.setTaskId(taskId);
            vo.setTaskName(task.getTaskName());
            vo.setStudentUserId(s.getUserId());
            vo.setStudentName(u != null ? u.getRealName() : "");
            vo.setStudentEmail(u != null ? u.getEmail() : "");
            vo.setSubmitType(s.getSubmitType());
            vo.setStatus(s.getStatus());
            vo.setSubmitCount(s.getSubmitCount());
            vo.setIsLate(s.getIsLate());
            vo.setSubmittedAt(s.getSubmitTime());
            vo.setHasReview(reviewedSubmissionIds.contains(s.getId()));
            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, submissionPage.getTotal());
    }

    @Override
    public SubmissionDetailVO getSubmissionDetail(Long submissionId) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提交记录不存在");
        }

        TrainingTask task = trainingTaskMapper.selectById(submission.getTrainingTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该提交");
        }

        User student = userMapper.selectById(submission.getUserId());

        SubmissionDetailVO vo = new SubmissionDetailVO();
        vo.setSubmissionId(submission.getId());
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setStudentUserId(submission.getUserId());
        vo.setStudentName(student != null ? student.getRealName() : "");
        vo.setSubmitType(submission.getSubmitType());
        vo.setStatus(submission.getStatus());
        vo.setGitUrl(submission.getGitUrl());
        vo.setGitBranch(submission.getGitBranch());
        vo.setSummary(submission.getSummary());
        vo.setSubmitCount(submission.getSubmitCount());
        vo.setIsLate(submission.getIsLate());
        vo.setSubmittedAt(submission.getSubmitTime());

        List<SubmissionFile> submissionFiles = submissionFileMapper.selectList(
                new LambdaQueryWrapper<SubmissionFile>()
                        .eq(SubmissionFile::getSubmissionId, submissionId)
                        .orderByAsc(SubmissionFile::getSortOrder));

        List<SubmissionDetailVO.AttachmentVO> attachments = new ArrayList<>();
        if (!submissionFiles.isEmpty()) {
            List<Long> fileIds = submissionFiles.stream().map(SubmissionFile::getFileId).toList();
            Map<Long, FileStorage> fileMap = fileStorageMapper.selectBatchIds(fileIds).stream()
                    .collect(Collectors.toMap(FileStorage::getId, f -> f, (a, b) -> a));

            for (SubmissionFile sf : submissionFiles) {
                SubmissionDetailVO.AttachmentVO att = new SubmissionDetailVO.AttachmentVO();
                att.setFileId(sf.getFileId());
                att.setFileName(sf.getFileName());
                att.setFileSize(sf.getFileSize());
                att.setFileType(sf.getFileType());
                FileStorage fs = fileMap.get(sf.getFileId());
                if (fs != null) {
                    att.setDownloadUrl(fs.getAccessUrl());
                }
                attachments.add(att);
            }
        }
        vo.setAttachments(attachments);

        TeacherReview review = teacherReviewMapper.selectOne(
                new LambdaQueryWrapper<TeacherReview>()
                        .eq(TeacherReview::getSubmissionId, submissionId)
                        .orderByDesc(TeacherReview::getId)
                        .last("LIMIT 1"));

        if (review != null) {
            SubmissionDetailVO.TeacherReviewVO reviewVO = new SubmissionDetailVO.TeacherReviewVO();
            reviewVO.setReviewId(review.getId());
            reviewVO.setComment(review.getTeacherComment());
            reviewVO.setStatus(review.getStatus());
            reviewVO.setReviewTime(review.getReviewTime());

            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>()
                            .eq(ScoreRecord::getSubmissionId, submissionId));
            if (!scores.isEmpty()) {
                reviewVO.setTotalScore(scores.get(0).getTotalScore());
            }

            vo.setReview(reviewVO);
        }

        AiAnalysis aiAnalysis = aiAnalysisMapper.selectOne(
                new LambdaQueryWrapper<AiAnalysis>()
                        .eq(AiAnalysis::getSubmissionId, submissionId)
                        .eq(AiAnalysis::getAnalysisStatus, "COMPLETED")
                        .orderByDesc(AiAnalysis::getId)
                        .last("LIMIT 1"));

        if (aiAnalysis != null) {
            List<AiAnalysisDetail> details = aiAnalysisDetailMapper.selectList(
                    new LambdaQueryWrapper<AiAnalysisDetail>()
                            .eq(AiAnalysisDetail::getAiAnalysisId, aiAnalysis.getId())
                            .orderByAsc(AiAnalysisDetail::getSortOrder));

            List<SubmissionDetailVO.AiDimensionVO> aiDims = new ArrayList<>();
            for (AiAnalysisDetail detail : details) {
                SubmissionDetailVO.AiDimensionVO dim = new SubmissionDetailVO.AiDimensionVO();
                dim.setComment(detail.getReason());

                if (detail.getSuggestDeduct() != null) {
                    dim.setScore(BigDecimal.valueOf(100).subtract(detail.getSuggestDeduct()).max(BigDecimal.ZERO));
                }

                if (detail.getDimensionId() != null) {
                    StandardDimension sd = standardDimensionMapper.selectById(detail.getDimensionId());
                    if (sd != null) {
                        dim.setDimensionName(sd.getDimName());
                    }
                }
                aiDims.add(dim);
            }
            vo.setAiDimensions(aiDims);
        } else {
            vo.setAiDimensions(Collections.emptyList());
        }

        return vo;
    }

    @Override
    public void reviewSubmission(Long submissionId, ReviewSubmitDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提交记录不存在");
        }

        TrainingTask task = trainingTaskMapper.selectById(submission.getTrainingTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该提交");
        }

        TeacherReview existingReview = teacherReviewMapper.selectOne(
                new LambdaQueryWrapper<TeacherReview>()
                        .eq(TeacherReview::getSubmissionId, submissionId)
                        .orderByDesc(TeacherReview::getId)
                        .last("LIMIT 1"));

        if (existingReview != null && "PUBLISHED".equals(existingReview.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已发布批阅结果，不可重复批阅");
        }

        TeacherReview review;
        if (existingReview != null && !"PUBLISHED".equals(existingReview.getStatus())) {
            review = existingReview;
        } else {
            review = new TeacherReview();
            review.setSubmissionId(submissionId);
        }
        review.setReviewerId(teacherId);
        review.setTeacherComment(dto.getTeacherComment());
        review.setStatus(dto.getStatus());
        review.setReviewTime(LocalDateTime.now());

        if ("PUBLISHED".equals(dto.getStatus())) {
            review.setPublishTime(LocalDateTime.now());
        }

        if (review.getId() != null) { teacherReviewMapper.updateById(review); } else { teacherReviewMapper.insert(review); }

        ScoreRecord existingScore = scoreRecordMapper.selectOne(
                new LambdaQueryWrapper<ScoreRecord>()
                        .eq(ScoreRecord::getSubmissionId, submissionId));

        ScoreRecord sr;
        if (existingScore != null) {
            sr = existingScore;
        } else {
            sr = new ScoreRecord();
            sr.setSubmissionId(submissionId);
            sr.setUserId(submission.getUserId());
            sr.setTrainingTaskId(submission.getTrainingTaskId());
        }
        sr.setStatus(dto.getStatus());

        if (dto.getDimensions() != null && !dto.getDimensions().isEmpty()) {
            BigDecimal totalScore = dto.getDimensions().stream()
                    .map(ReviewSubmitDTO.DimensionScoreDTO::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dto.getDimensions().size()), 2, java.math.RoundingMode.HALF_UP);
            sr.setTotalScore(totalScore);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < dto.getDimensions().size(); i++) {
                ReviewSubmitDTO.DimensionScoreDTO dim = dto.getDimensions().get(i);
                if (i > 0) json.append(",");
                json.append("{\"dimensionId\":").append(dim.getDimensionId())
                    .append(",\"score\":").append(dim.getScore())
                    .append(",\"remark\":\"").append(dim.getRemark() != null ? dim.getRemark() : "").append("\"}");
            }
            json.append("]");
            sr.setScoreDetails(json.toString());
        }

        if (sr.getId() != null) { scoreRecordMapper.updateById(sr); } else { scoreRecordMapper.insert(sr); }

        if ("PUBLISHED".equals(dto.getStatus())) {
            submission.setStatus("REVIEWED");
        } else if ("REJECTED".equals(dto.getStatus())) {
            submission.setStatus("REJECTED");
        }
        submissionMapper.updateById(submission);
    }

    @Override
    public PageResult<SubmissionListVO> listAllSubmissions(int page, int pageSize, String status, String keyword) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        List<Long> courseIds = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getUserId, teacherId))
                .stream().map(CourseTeacher::getCourseId).distinct().toList();

        if (courseIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        List<TrainingTask> tasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>()
                        .in(TrainingTask::getCourseId, courseIds)
                        .eq(TrainingTask::getDeleted, 0));

        if (tasks.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        List<Long> taskIds = tasks.stream().map(TrainingTask::getId).toList();
        Map<Long, TrainingTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(TrainingTask::getId, t -> t, (a, b) -> a));

        LambdaQueryWrapper<Submission> query = new LambdaQueryWrapper<Submission>()
                .in(Submission::getTrainingTaskId, taskIds)
                .eq(Submission::getDeleted, 0)
                .eq(Submission::getStatus, "SUBMITTED");
        if (StringUtils.hasText(status)) {
            query.eq(Submission::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            List<User> matchedUsers = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getRealName, keyword));
            if (!matchedUsers.isEmpty()) {
                List<Long> userIds = matchedUsers.stream().map(User::getId).toList();
                query.in(Submission::getUserId, userIds);
            }
        }
        query.orderByDesc(Submission::getSubmitTime);

        IPage<Submission> submissionPage = submissionMapper.selectPage(
                new Page<>(page, pageSize), query);

        List<Submission> submissions = submissionPage.getRecords();
        List<Long> userIds = submissions.stream().map(Submission::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
        Set<Long> reviewedSubmissionIds = Collections.emptySet();
        if (!submissionIds.isEmpty()) {
            reviewedSubmissionIds = teacherReviewMapper.selectList(
                    new LambdaQueryWrapper<TeacherReview>()
                            .in(TeacherReview::getSubmissionId, submissionIds))
                    .stream().map(TeacherReview::getSubmissionId).collect(Collectors.toSet());
        }

        List<SubmissionListVO> vos = new ArrayList<>();
        for (Submission s : submissions) {
            User u = userMap.get(s.getUserId());
            TrainingTask task = taskMap.get(s.getTrainingTaskId());
            SubmissionListVO vo = new SubmissionListVO();
            vo.setSubmissionId(s.getId());
            vo.setTaskId(s.getTrainingTaskId());
            vo.setTaskName(task != null ? task.getTaskName() : "");
            vo.setStudentUserId(s.getUserId());
            vo.setStudentName(u != null ? u.getRealName() : "");
            vo.setStudentEmail(u != null ? u.getEmail() : "");
            vo.setSubmitType(s.getSubmitType());
            vo.setStatus(s.getStatus());
            vo.setSubmitCount(s.getSubmitCount());
            vo.setIsLate(s.getIsLate());
            vo.setSubmittedAt(s.getSubmitTime());
            vo.setHasReview(reviewedSubmissionIds.contains(s.getId()));
            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, submissionPage.getTotal());
    }

    @Override
    public AiResultVO getSubmissionAiResult(Long submissionId) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "提交记录不存在");
        }

        TrainingTask task = trainingTaskMapper.selectById(submission.getTrainingTaskId());
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该提交");
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

        AiResultVO vo = new AiResultVO();
        vo.setAnalyzeId(aiAnalysis.getId());
        vo.setStatus(aiAnalysis.getAnalysisStatus());
        vo.setStartedAt(aiAnalysis.getStartTime());
        vo.setCompletedAt(aiAnalysis.getCompleteTime());

        switch (aiAnalysis.getAnalysisStatus()) {
            case "PENDING":
                vo.setProgress(0);
                break;
            case "PROCESSING":
                vo.setProgress(50);
                vo.setCurrentDimension("代码规范");
                break;
            case "COMPLETED": {
                vo.setProgress(100);
                List<AiAnalysisDetail> details = aiAnalysisDetailMapper.selectList(
                        new LambdaQueryWrapper<AiAnalysisDetail>()
                                .eq(AiAnalysisDetail::getAiAnalysisId, aiAnalysis.getId())
                                .orderByAsc(AiAnalysisDetail::getSortOrder));

                List<AiResultVO.AiDimensionScoreVO> dimVOs = new ArrayList<>();
                for (AiAnalysisDetail detail : details) {
                    AiResultVO.AiDimensionScoreVO dv = new AiResultVO.AiDimensionScoreVO();
                    dv.setComment(detail.getReason());
                    dv.setSuggestions(detail.getSuggestion() != null
                            ? List.of(detail.getSuggestion()) : Collections.emptyList());
                    dv.setCodeReferences(Collections.emptyList());
                    if (detail.getSuggestDeduct() != null) {
                        dv.setScore(BigDecimal.valueOf(100).subtract(detail.getSuggestDeduct()).max(BigDecimal.ZERO));
                    }
                    dv.setAgentType(detail.getAgentType());
                    dv.setIssueType(detail.getIssueType());
                    dv.setSuggestDeduct(detail.getSuggestDeduct());
                    dv.setFilePath(detail.getFilePath());
                    dv.setLineNumber(detail.getLineNumber());
                    dv.setConfidence(detail.getConfidence());
                    if (detail.getDimensionId() != null) {
                        StandardDimension sd = standardDimensionMapper.selectById(detail.getDimensionId());
                        if (sd != null) {
                            dv.setDimensionName(sd.getDimName());
                            dv.setMaxScore(sd.getMaxScore());
                            dv.setWeight(sd.getWeight());
                        }
                    }
                    dimVOs.add(dv);
                }

                AiResultVO.AiScoreResultVO result = new AiResultVO.AiScoreResultVO();
                result.setOverallScore(aiAnalysis.getTotalScore());
                result.setDimensions(dimVOs);
                result.setSummary(aiAnalysis.getRawResponse());
                result.setStrengths(Collections.emptyList());
                result.setWeaknesses(Collections.emptyList());
                result.setImprovementPlan("");
                vo.setResult(result);
                break;
            }
            case "FAILED":
                vo.setProgress(0);
                break;
            default:
                vo.setProgress(0);
                break;
        }

        return vo;
    }
}

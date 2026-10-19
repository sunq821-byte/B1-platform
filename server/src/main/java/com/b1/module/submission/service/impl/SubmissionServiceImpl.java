package com.b1.module.submission.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.ai.entity.AiAnalysis;
import com.b1.module.ai.mapper.AiAnalysisMapper;
import com.b1.module.file.entity.FileStorage;
import com.b1.module.file.mapper.FileStorageMapper;
import com.b1.module.submission.dto.ConfirmSubmitDTO;
import com.b1.module.submission.dto.SubmitRequestDTO;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.entity.SubmissionFile;
import com.b1.module.submission.entity.SubmissionTrial;
import com.b1.module.submission.mapper.SubmissionFileMapper;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.submission.mapper.SubmissionTrialMapper;
import com.b1.module.submission.service.SubmissionService;
import com.b1.module.submission.vo.GitVerifyResultVO;
import com.b1.module.submission.vo.ReportUploadVO;
import com.b1.module.submission.vo.SubmissionDraftVO;
import com.b1.module.submission.vo.SubmissionHistoryVO;
import com.b1.module.submission.vo.SubmissionVO;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.file.service.FileService;
import com.b1.module.file.vo.FileUploadVO;
import com.b1.module.submission.dto.GitVerifyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private static final Set<String> LOCKED_STATUSES = Set.of("CONFIRMED", "REVIEWING", "PUBLISHED");
    private static final Set<String> TEACHER_VISIBLE = Set.of("CONFIRMED", "REVIEWING", "PUBLISHED", "INCOMPLETE", "REJECTED");

    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionTrialMapper submissionTrialMapper;
    private final FileService fileService;
    private final AiAnalysisMapper aiAnalysisMapper;
    private final SubmissionFileMapper submissionFileMapper;
    private final FileStorageMapper fileStorageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmissionVO submit(Long taskId, SubmitRequestDTO dto) {
        return trialSubmit(taskId, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmissionVO trialSubmit(Long taskId, SubmitRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        TrainingTask task = requireOpenTask(taskId);
        if (dto.getSubmissionType() != null && dto.getSubmissionType().toUpperCase().contains("GIT")) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前版本不支持 Git 仓库提交");
        }
        LocalDateTime now = LocalDateTime.now();
        int maxAi = task.getMaxAiAnalyzeCount() != null ? task.getMaxAiAnalyzeCount() : 3;

        Submission submission = submissionMapper.selectOne(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));

        if (submission != null && LOCKED_STATUSES.contains(submission.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已确认提交，无法继续试交");
        }
        if (submission != null && "ANALYZING".equals(submission.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "AI 分析进行中，请稍后再试交");
        }

        int usedCount = submission != null && submission.getAiAnalyzeUsedCount() != null
                ? submission.getAiAnalyzeUsedCount() : 0;
        boolean rejected = submission != null && "REJECTED".equals(submission.getStatus());
        if (!rejected && usedCount >= maxAi) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "已达到 AI 分析次数上限（" + maxAi + "次），请确认提交或联系教师驳回重提");
        }

        if (submission == null) {
            submission = new Submission();
            submission.setTrainingTaskId(taskId);
            submission.setUserId(userId);
            submission.setSubmitType(dto.getSubmissionType());
            submission.setSubmitCount(0);
            submission.setAiAnalyzeUsedCount(0);
            submission.setStatus("DRAFT");
            submissionMapper.insert(submission);
        }

        int trialNo = usedCount + 1;
        SubmissionTrial trial = new SubmissionTrial();
        trial.setSubmissionId(submission.getId());
        trial.setTrialNo(trialNo);
        trial.setSubmitType(dto.getSubmissionType());
        trial.setGitUrl(dto.getGitUrl());
        trial.setGitBranch(dto.getGitBranch());
        trial.setSummary(dto.getRemark());
        trial.setSubmitTime(now);
        trial.setStatus("ANALYZING");
        submissionTrialMapper.insert(trial);

        submission.setSubmitType(dto.getSubmissionType());
        submission.setGitUrl(dto.getGitUrl());
        submission.setGitBranch(dto.getGitBranch());
        submission.setSummary(dto.getRemark());
        submission.setSubmitCount(trialNo);
        submission.setAiAnalyzeUsedCount(trialNo);
        submission.setSubmitTime(now);
        submission.setIsLate((task.getEndTime() != null && now.isAfter(task.getEndTime())) ? 1 : 0);
        submission.setStatus("ANALYZING");
        submission.setCurrentTrialId(trial.getId());
        submission.setConfirmedTrialId(null);
        submission.setConfirmedAt(null);
        submissionMapper.updateById(submission);

        linkSubmissionFile(submission.getId(), dto);

        return buildSubmissionVO(submission, trial, task, maxAi);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmissionVO confirmSubmit(Long taskId, ConfirmSubmitDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        TrainingTask task = requireOpenTask(taskId);
        int maxAi = task.getMaxAiAnalyzeCount() != null ? task.getMaxAiAnalyzeCount() : 3;

        Submission submission = submissionMapper.selectOne(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));
        if (submission == null || submission.getCurrentTrialId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "请先试交成果后再确认提交");
        }
        if (LOCKED_STATUSES.contains(submission.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "已确认提交，请勿重复操作");
        }

        Long trialId = dto != null && dto.getTrialId() != null ? dto.getTrialId() : submission.getCurrentTrialId();
        SubmissionTrial trial = submissionTrialMapper.selectById(trialId);
        if (trial == null || !trial.getSubmissionId().equals(submission.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "试交记录不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        trial.setStatus("CONFIRMED");
        submissionTrialMapper.updateById(trial);

        submission.setStatus("CONFIRMED");
        submission.setConfirmedTrialId(trialId);
        submission.setConfirmedAt(now);
        submission.setIsLate((task.getEndTime() != null && now.isAfter(task.getEndTime())) ? 1 : 0);
        submissionMapper.updateById(submission);

        return buildSubmissionVO(submission, trial, task, maxAi);
    }

    private TrainingTask requireOpenTask(Long taskId) {
        TrainingTask task = trainingTaskMapper.selectOne(
                new LambdaQueryWrapper<TrainingTask>()
                        .eq(TrainingTask::getId, taskId)
                        .eq(TrainingTask::getDeleted, 0)
                        .eq(TrainingTask::getStatus, "PUBLISHED"));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在或已下架");
        }
        LocalDateTime now = LocalDateTime.now();
        if (task.getEndTime() != null && now.isAfter(task.getEndTime())
                && (task.getAllowLate() == null || task.getAllowLate() != 1)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该实训任务已截止");
        }
        return task;
    }

    private void linkSubmissionFile(Long submissionId, SubmitRequestDTO dto) {
        submissionFileMapper.delete(new LambdaQueryWrapper<SubmissionFile>()
                .eq(SubmissionFile::getSubmissionId, submissionId));

        String zipFileId = dto.getZipFileId();
        if (zipFileId == null || zipFileId.isBlank()) {
            return;
        }

        long fileId;
        try {
            fileId = Long.parseLong(zipFileId.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.PARAM_FORMAT_ERROR, "文件ID格式错误");
        }

        FileStorage storage = fileStorageMapper.selectById(fileId);
        if (storage == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        SubmissionFile sf = new SubmissionFile();
        sf.setSubmissionId(submissionId);
        sf.setFileId(fileId);
        sf.setFileName(storage.getOriginalName());
        sf.setFileType(storage.getContentType());
        sf.setFileSize(storage.getFileSize());
        sf.setSortOrder(1);
        submissionFileMapper.insert(sf);
    }

    private SubmissionVO buildSubmissionVO(Submission submission, SubmissionTrial trial,
                                           TrainingTask task, int maxAi) {
        SubmissionVO vo = new SubmissionVO();
        vo.setSubmissionId(submission.getId());
        vo.setTrialId(trial.getId());
        vo.setTaskId(task.getId());
        vo.setSubmissionType(trial.getSubmitType());
        vo.setStatus(submission.getStatus());
        vo.setTrialNo(trial.getTrialNo());
        vo.setAiAnalyzeUsedCount(submission.getAiAnalyzeUsedCount());
        vo.setMaxAiAnalyzeCount(maxAi);
        vo.setMaxSubmitCount(maxAi);
        vo.setSubmittedAt(trial.getSubmitTime());
        vo.setConfirmedAt(submission.getConfirmedAt());
        return vo;
    }

    @Override
    public SubmissionDraftVO getDraft(Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Submission submission = submissionMapper.selectOne(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));
        if (submission == null) {
            return null;
        }

        SubmissionDraftVO vo = new SubmissionDraftVO();
        vo.setSubmissionId(submission.getId());
        vo.setSubmitType(submission.getSubmitType());
        vo.setStatus(submission.getStatus());
        vo.setRemark(submission.getSummary());

        String submitType = submission.getSubmitType() != null
                ? submission.getSubmitType().toUpperCase() : "";
        if (!submitType.contains("CODE")) {
            return vo;
        }

        SubmissionFile primaryFile = submissionFileMapper.selectOne(
                new LambdaQueryWrapper<SubmissionFile>()
                        .eq(SubmissionFile::getSubmissionId, submission.getId())
                        .orderByAsc(SubmissionFile::getSortOrder)
                        .last("LIMIT 1"));
        if (primaryFile == null || primaryFile.getFileId() == null) {
            return vo;
        }

        try (InputStream in = fileService.download(primaryFile.getFileId())) {
            vo.setOnlineCode(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 文件读取失败时仍返回备注等元数据
        }
        return vo;
    }

    @Override
    public List<SubmissionHistoryVO> listHistory(Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Submission submission = submissionMapper.selectOne(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));
        if (submission == null) {
            return List.of();
        }

        List<SubmissionTrial> trials = submissionTrialMapper.selectList(
                new LambdaQueryWrapper<SubmissionTrial>()
                        .eq(SubmissionTrial::getSubmissionId, submission.getId())
                        .orderByAsc(SubmissionTrial::getTrialNo));

        List<SubmissionHistoryVO> result = new ArrayList<>();
        for (SubmissionTrial trial : trials) {
            SubmissionHistoryVO vo = new SubmissionHistoryVO();
            vo.setSubmissionId(submission.getId());
            vo.setTrialId(trial.getId());
            vo.setStatus(trial.getStatus());
            vo.setSubmitCount(trial.getTrialNo());
            vo.setSubmittedAt(trial.getSubmitTime());
            if (trial.getAiReferenceScore() != null) {
                vo.setAiScore(trial.getAiReferenceScore());
            } else {
                AiAnalysis analysis = aiAnalysisMapper.selectOne(
                        new LambdaQueryWrapper<AiAnalysis>()
                                .eq(AiAnalysis::getTrialId, trial.getId())
                                .eq(AiAnalysis::getAnalysisStatus, "COMPLETED")
                                .last("LIMIT 1"));
                if (analysis != null) {
                    vo.setAiScore(analysis.getTotalScore());
                }
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public GitVerifyResultVO verifyGit(Long taskId, GitVerifyDTO dto) {
        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        String gitUrl = dto.getGitUrl();
        String repoName = extractRepoName(gitUrl);
        String defaultBranch = dto.getGitBranch() != null ? dto.getGitBranch() : "main";

        GitVerifyResultVO.GitCommitVO commitVO = new GitVerifyResultVO.GitCommitVO();
        commitVO.setCommitId("abc123def456789012345678901234567890abcd");
        commitVO.setShorter("abc123d");
        commitVO.setMessage("feat: initial commit");
        commitVO.setAuthor("student");
        commitVO.setCommittedAt(LocalDateTime.now());

        GitVerifyResultVO vo = new GitVerifyResultVO();
        vo.setValid(true);
        vo.setRepoName(repoName);
        vo.setDefaultBranch(defaultBranch);
        vo.setBranches(List.of(defaultBranch));
        vo.setLatestCommit(commitVO);
        return vo;
    }

    @Override
    public ReportUploadVO uploadReport(Long taskId, MultipartFile file, String title) {
        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        FileUploadVO upload = fileService.upload(file, "reports");

        ReportUploadVO vo = new ReportUploadVO();
        vo.setFileId(upload.getFileId());
        vo.setFileName(upload.getOriginalName());
        vo.setFileSize(upload.getFileSize());
        vo.setFileType(file.getContentType());
        vo.setUploadedAt(LocalDateTime.now());
        return vo;
    }

    private String extractRepoName(String gitUrl) {
        if (gitUrl == null || gitUrl.isBlank()) {
            return "unknown";
        }
        String name = gitUrl.substring(gitUrl.lastIndexOf('/') + 1);
        if (name.endsWith(".git")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }
}

package com.b1.module.submission.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.file.service.FileService;
import com.b1.module.file.vo.FileUploadVO;
import com.b1.module.submission.dto.GitVerifyDTO;
import com.b1.module.submission.dto.SubmitRequestDTO;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.submission.service.SubmissionService;
import com.b1.module.submission.vo.GitVerifyResultVO;
import com.b1.module.submission.vo.ReportUploadVO;
import com.b1.module.submission.vo.SubmissionVO;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;
    private final FileService fileService;

    @Override
    public SubmissionVO submit(Long taskId, SubmitRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (task.getEndTime() != null && now.isAfter(task.getEndTime())
                && (task.getAllowLate() == null || task.getAllowLate() != 1)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该实训任务已截止");
        }

        // Check for an existing REJECTED submission that can be re-submitted
        Submission rejectedSubmission = submissionMapper.selectOne(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getStatus, "REJECTED")
                        .eq(Submission::getDeleted, 0));

        if (rejectedSubmission != null) {
            // Re-submit: update the existing rejected submission in-place
            int newSubmitCount = (rejectedSubmission.getSubmitCount() != null ? rejectedSubmission.getSubmitCount() : 0) + 1;
            int isLate = (task.getEndTime() != null && now.isAfter(task.getEndTime())) ? 1 : 0;

            rejectedSubmission.setSubmitType(dto.getSubmissionType());
            rejectedSubmission.setGitUrl(dto.getGitUrl());
            rejectedSubmission.setGitBranch(dto.getGitBranch());
            rejectedSubmission.setSummary(dto.getRemark());
            rejectedSubmission.setSubmitCount(newSubmitCount);
            rejectedSubmission.setSubmitTime(now);
            rejectedSubmission.setIsLate(isLate);
            rejectedSubmission.setStatus("SUBMITTED");

            submissionMapper.updateById(rejectedSubmission);

            SubmissionVO vo = new SubmissionVO();
            vo.setSubmissionId(rejectedSubmission.getId());
            vo.setTaskId(taskId);
            vo.setSubmissionType(dto.getSubmissionType());
            vo.setStatus("SUBMITTED");
            vo.setSubmitCount(newSubmitCount);
            vo.setMaxSubmitCount(task.getMaxSubmitCount() != null ? task.getMaxSubmitCount() : 1);
            vo.setSubmittedAt(now);

            return vo;
        }

        Long existingCount = submissionMapper.selectCount(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));

        int maxSubmitCount = task.getMaxSubmitCount() != null ? task.getMaxSubmitCount() : 1;
        if (existingCount >= maxSubmitCount) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED,
                    "已达到提交次数上限（" + maxSubmitCount + "次），无法再次提交");
        }

        int newSubmitCount = existingCount.intValue() + 1;

        int isLate = (task.getEndTime() != null && now.isAfter(task.getEndTime())) ? 1 : 0;

        Submission submission = new Submission();
        submission.setTrainingTaskId(taskId);
        submission.setUserId(userId);
        submission.setSubmitType(dto.getSubmissionType());
        submission.setGitUrl(dto.getGitUrl());
        submission.setGitBranch(dto.getGitBranch());
        submission.setSummary(dto.getRemark());
        submission.setSubmitCount(newSubmitCount);
        submission.setSubmitTime(now);
        submission.setIsLate(isLate);
        submission.setStatus("SUBMITTED");

        submissionMapper.insert(submission);

        SubmissionVO vo = new SubmissionVO();
        vo.setSubmissionId(submission.getId());
        vo.setTaskId(taskId);
        vo.setSubmissionType(dto.getSubmissionType());
        vo.setStatus("SUBMITTED");
        vo.setSubmitCount(newSubmitCount);
        vo.setMaxSubmitCount(maxSubmitCount);
        vo.setSubmittedAt(now);

        return vo;
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
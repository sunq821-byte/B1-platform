package com.b1.module.submission.service;

import com.b1.module.submission.dto.ConfirmSubmitDTO;
import com.b1.module.submission.dto.GitVerifyDTO;
import com.b1.module.submission.dto.SubmitRequestDTO;
import com.b1.module.submission.vo.GitVerifyResultVO;
import com.b1.module.submission.vo.ReportUploadVO;
import com.b1.module.submission.vo.SubmissionDraftVO;
import com.b1.module.submission.vo.SubmissionHistoryVO;
import com.b1.module.submission.vo.SubmissionVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SubmissionService {

    /** @deprecated 请使用 {@link #trialSubmit} */
    SubmissionVO submit(Long taskId, SubmitRequestDTO dto);

    SubmissionVO trialSubmit(Long taskId, SubmitRequestDTO dto);

    SubmissionVO confirmSubmit(Long taskId, ConfirmSubmitDTO dto);

    List<SubmissionHistoryVO> listHistory(Long taskId);

    SubmissionDraftVO getDraft(Long taskId);

    GitVerifyResultVO verifyGit(Long taskId, GitVerifyDTO dto);

    ReportUploadVO uploadReport(Long taskId, MultipartFile file, String title);
}
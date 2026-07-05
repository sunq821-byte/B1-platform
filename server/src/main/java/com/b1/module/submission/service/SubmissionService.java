package com.b1.module.submission.service;

import com.b1.module.submission.dto.GitVerifyDTO;
import com.b1.module.submission.dto.SubmitRequestDTO;
import com.b1.module.submission.vo.GitVerifyResultVO;
import com.b1.module.submission.vo.ReportUploadVO;
import com.b1.module.submission.vo.SubmissionVO;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {

    SubmissionVO submit(Long taskId, SubmitRequestDTO dto);

    GitVerifyResultVO verifyGit(Long taskId, GitVerifyDTO dto);

    ReportUploadVO uploadReport(Long taskId, MultipartFile file, String title);
}
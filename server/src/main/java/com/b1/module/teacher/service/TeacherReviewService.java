package com.b1.module.teacher.service;

import com.b1.common.result.PageResult;
import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.teacher.dto.ReviewSubmitDTO;
import com.b1.module.teacher.vo.SubmissionDetailVO;
import com.b1.module.teacher.vo.SubmissionListVO;

public interface TeacherReviewService {

    PageResult<SubmissionListVO> listSubmissions(Long taskId, int page, int pageSize, String status, String keyword);

    SubmissionDetailVO getSubmissionDetail(Long submissionId);

    void reviewSubmission(Long submissionId, ReviewSubmitDTO dto);

    AiResultVO getSubmissionAiResult(Long submissionId);
}

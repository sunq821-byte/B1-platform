package com.b1.module.ai.service;

import com.b1.module.ai.vo.AiResultVO;
import com.b1.module.ai.vo.EvaluationVO;

public interface AiService {

    AiResultVO initiateEvaluation(Long submissionId);

    AiResultVO getAiResult(Long submissionId);

    EvaluationVO getFullEvaluation(Long submissionId);
}
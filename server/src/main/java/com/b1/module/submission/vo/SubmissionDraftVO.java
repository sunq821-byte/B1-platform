package com.b1.module.submission.vo;

import lombok.Data;

@Data
public class SubmissionDraftVO {

    private Long submissionId;

    /** 提交方式：ZIP_UPLOAD / ONLINE_CODE 等 */
    private String submitType;

    private String status;

    /** 提交备注 */
    private String remark;

    /** 在线代码内容（仅 ONLINE_CODE 时有值） */
    private String onlineCode;
}

package com.b1.module.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitRequestDTO {

    @NotBlank(message = "提交类型不能为空")
    private String submissionType;

    private String gitUrl;

    private String gitBranch;

    private String zipFileId;

    private String onlineCode;

    @Size(max = 500, message = "备注最长500字符")
    private String remark;
}
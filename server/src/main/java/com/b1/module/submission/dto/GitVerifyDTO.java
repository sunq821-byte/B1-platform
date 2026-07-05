package com.b1.module.submission.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GitVerifyDTO {

    @NotBlank(message = "Git仓库地址不能为空")
    private String gitUrl;

    private String gitBranch;

    private String accessToken;
}
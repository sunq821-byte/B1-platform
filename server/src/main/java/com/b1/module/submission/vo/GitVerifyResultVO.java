package com.b1.module.submission.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GitVerifyResultVO {

    private Boolean valid;

    private String repoName;

    private String defaultBranch;

    private List<String> branches;

    private GitCommitVO latestCommit;

    @Data
    public static class GitCommitVO {

        private String commitId;

        private String shorter;

        private String message;

        private String author;

        private LocalDateTime committedAt;
    }
}
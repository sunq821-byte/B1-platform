package com.b1.common.enums;

import lombok.Getter;

@Getter
public enum SubmissionStatus {

    SUBMITTED("已提交"),
    ANALYZING("AI分析中"),
    COMPLETED("分析完成"),
    REVIEWED("教师已评分"),
    REJECTED("已退回");

    private final String description;

    SubmissionStatus(String description) {
        this.description = description;
    }
}

package com.b1.common.enums;

import lombok.Getter;

@Getter
public enum AnalysisStatus {

    PENDING("排队中"),
    PROCESSING("分析中"),
    COMPLETED("已完成"),
    FAILED("失败");

    private final String description;

    AnalysisStatus(String description) {
        this.description = description;
    }
}

package com.b1.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSystemConfigVO {

    private String systemName;
    private String currentSemester;
    private String semesterStart;
    private String semesterEnd;
    private int maxUploadSize;
    private String aiModelVersion;
    private boolean autoAnalyze;
    private boolean notificationEnabled;
    private boolean maintenanceMode;
}

package com.b1.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMonitorVO {

    private List<ServiceHealth> services;
    private int cpuUsage;
    private int memoryUsage;
    private int diskUsage;
    private String uptime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String name;
        private String status;
        private String color;
    }
}

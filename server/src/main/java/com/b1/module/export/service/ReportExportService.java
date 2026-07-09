package com.b1.module.export.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ReportExportService {

    /**
     * 导出班级成绩报表。
     *
     * @param className 班级名（null/空 表示全部班级）
     * @param format    xlsx | pdf
     */
    void exportClassReport(String className, String format, HttpServletResponse response);

    /**
     * 导出当前登录学生的个人成绩报告。
     *
     * @param format xlsx | pdf
     */
    void exportStudentReport(String format, HttpServletResponse response);
}

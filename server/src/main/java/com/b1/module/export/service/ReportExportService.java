package com.b1.module.export.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ReportExportService {

    /**
     * 导出班级成绩报表。
     *
     * @param adminClass  行政班名（null/空 表示全部）
     * @param courseName  课程名（null/空 表示全部）
     * @param format      xlsx | pdf
     */
    void exportClassReport(String adminClass, String courseName, String format, HttpServletResponse response);

    /**
     * 导出当前登录学生的个人成绩报告。
     *
     * @param format xlsx | pdf
     */
    void exportStudentReport(String format, HttpServletResponse response);
}

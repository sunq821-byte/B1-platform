package com.b1.module.export.service.impl;

import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.export.service.ReportExportService;
import com.b1.module.export.util.ExcelExporter;
import com.b1.module.export.util.PdfExporter;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.StudentReportVO;
import com.b1.module.teacher.service.TeacherReportService;
import com.b1.module.teacher.vo.ClassReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {

    private static final String FMT_XLSX = "xlsx";
    private static final String FMT_PDF = "pdf";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TeacherReportService teacherReportService;
    private final StudentService studentService;

    @Override
    public void exportClassReport(String className, String format, HttpServletResponse response) {
        String fmt = normalizeFormat(format);
        ClassReportVO vo = teacherReportService.getClassReport(className);
        String classLabel = StringUtils.hasText(className) ? className : "全部班级";
        String fileName = "班级成绩报表_" + classLabel + "_" + LocalDate.now().format(DATE);

        List<String> summaryHeaders = List.of("统计项", "数值");
        List<List<Object>> summaryRows = new ArrayList<>();
        ClassReportVO.Stats stats = vo.getStats();
        summaryRows.add(List.of("班级人数", stats.getTotalStudents()));
        summaryRows.add(List.of("已评阅数", stats.getTotalReviewed()));
        summaryRows.add(List.of("班级均分", num(stats.getClassAverage())));
        summaryRows.add(List.of("及格率", num(stats.getPassRate()) + "%"));

        List<String> detailHeaders = List.of("学号", "姓名", "班级", "已完成", "平均分", "最高分", "最低分");
        List<List<Object>> detailRows = new ArrayList<>();
        for (ClassReportVO.Row r : vo.getRows()) {
            detailRows.add(List.of(
                    nz(r.getStudentId()),
                    nz(r.getName()),
                    nz(r.getClassName()),
                    r.getCompletedCount(),
                    num(r.getAvgScore()),
                    num(r.getMaxScore()),
                    num(r.getMinScore())
            ));
        }

        if (FMT_XLSX.equals(fmt)) {
            ExcelExporter.writeWorkbook(response, fileName, List.of(
                    new ExcelExporter.SheetData("统计摘要", summaryHeaders, summaryRows),
                    new ExcelExporter.SheetData("班级成绩", detailHeaders, detailRows)
            ));
        } else {
            PdfExporter.writeDocument(response, fileName, "班级成绩报表（" + classLabel + "）", List.of(
                    new PdfExporter.Section("统计摘要", summaryHeaders, toStringRows(summaryRows)),
                    new PdfExporter.Section("班级成绩表", detailHeaders, toStringRows(detailRows))
            ));
        }
    }

    @Override
    public void exportStudentReport(String format, HttpServletResponse response) {
        String fmt = normalizeFormat(format);
        StudentReportVO vo = studentService.getStudentReport();
        String fileName = "个人成绩报告_" + LocalDate.now().format(DATE);

        List<String> summaryHeaders = List.of("统计项", "数值");
        List<List<Object>> summaryRows = new ArrayList<>();
        StudentReportVO.Stats stats = vo.getStats();
        summaryRows.add(List.of("全部任务", stats.getTotalTasks()));
        summaryRows.add(List.of("已完成", stats.getCompletedTasks()));
        summaryRows.add(List.of("提交次数", stats.getTotalSubmissions()));
        summaryRows.add(List.of("平均成绩", num(stats.getAverageScore())));

        List<String> detailHeaders = List.of("任务名称", "课程", "成绩", "状态", "评语");
        List<List<Object>> detailRows = new ArrayList<>();
        for (StudentReportVO.Row r : vo.getRows()) {
            detailRows.add(List.of(
                    nz(r.getTaskName()),
                    nz(r.getCourseName()),
                    r.getScore() == null ? "-" : num(r.getScore()),
                    statusLabel(r.getStatus()),
                    nz(r.getReviewComment())
            ));
        }

        if (FMT_XLSX.equals(fmt)) {
            ExcelExporter.writeWorkbook(response, fileName, List.of(
                    new ExcelExporter.SheetData("统计摘要", summaryHeaders, summaryRows),
                    new ExcelExporter.SheetData("成绩明细", detailHeaders, detailRows)
            ));
        } else {
            PdfExporter.writeDocument(response, fileName, "个人成绩报告", List.of(
                    new PdfExporter.Section("统计摘要", summaryHeaders, toStringRows(summaryRows)),
                    new PdfExporter.Section("成绩明细", detailHeaders, toStringRows(detailRows))
            ));
        }
    }

    private String normalizeFormat(String format) {
        String fmt = format == null ? FMT_XLSX : format.trim().toLowerCase();
        if (!FMT_XLSX.equals(fmt) && !FMT_PDF.equals(fmt)) {
            throw new BusinessException(ErrorCode.PARAM_VALUE_ILLEGAL, "导出格式仅支持 xlsx 或 pdf");
        }
        return fmt;
    }

    private static List<List<String>> toStringRows(List<List<Object>> rows) {
        List<List<String>> out = new ArrayList<>();
        for (List<Object> row : rows) {
            List<String> line = new ArrayList<>();
            for (Object v : row) {
                line.add(v == null ? "" : String.valueOf(v));
            }
            out.add(line);
        }
        return out;
    }

    private static String num(BigDecimal v) {
        return v == null ? "0" : v.stripTrailingZeros().toPlainString();
    }

    private static String nz(String v) {
        return v == null ? "" : v;
    }

    private static String statusLabel(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "REVIEWED" -> "已评阅";
            case "REJECTED" -> "已退回";
            case "SUBMITTED" -> "已提交";
            default -> status;
        };
    }
}

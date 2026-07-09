package com.b1.module.export.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Excel 导出：将若干 sheet 的动态表头 + 行数据写入 HttpServletResponse。
 * 不依赖 @ExcelProperty 注解类，报表可复用同一套写出逻辑。
 */
public final class ExcelExporter {

    private ExcelExporter() {
    }

    public record SheetData(String sheetName, List<String> headers, List<List<Object>> rows) {
    }

    public static void writeWorkbook(HttpServletResponse response, String fileName, List<SheetData> sheets) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encoded = URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

        try (ExcelWriter writer = EasyExcel.write(response.getOutputStream())
                .autoCloseStream(false)
                .build()) {
            int index = 0;
            for (SheetData sheet : sheets) {
                WriteSheet writeSheet = EasyExcel.writerSheet(index++, sheet.sheetName())
                        .head(toHead(sheet.headers()))
                        .build();
                writer.write(sheet.rows(), writeSheet);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Excel 生成失败：" + e.getMessage());
        }
    }

    private static List<List<String>> toHead(List<String> headers) {
        List<List<String>> head = new ArrayList<>();
        for (String h : headers) {
            head.add(List.of(h));
        }
        return head;
    }
}

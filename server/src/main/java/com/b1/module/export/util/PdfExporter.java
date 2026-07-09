package com.b1.module.export.util;

import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 通用 PDF 导出：标题 + 若干 section（每个 section 一张表格）写入 HttpServletResponse。
 * 使用 OpenPDF 内置 STSong-Light CJK 字体渲染中文，无需打包 TTF。
 */
public final class PdfExporter {

    private static final BaseFont CJK_BASE;
    private static final Font TITLE_FONT;
    private static final Font HEADING_FONT;
    private static final Font TABLE_HEADER_FONT;
    private static final Font CELL_FONT;

    static {
        try {
            CJK_BASE = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new IllegalStateException("初始化 PDF 中文字体失败", e);
        }
        TITLE_FONT = new Font(CJK_BASE, 18, Font.BOLD);
        HEADING_FONT = new Font(CJK_BASE, 13, Font.BOLD);
        TABLE_HEADER_FONT = new Font(CJK_BASE, 10, Font.BOLD, Color.WHITE);
        CELL_FONT = new Font(CJK_BASE, 10, Font.NORMAL);
    }

    private PdfExporter() {
    }

    public record Section(String heading, List<String> headers, List<List<String>> rows) {
    }

    public static void writeDocument(HttpServletResponse response, String fileName, String title, List<Section> sections) {
        response.setContentType("application/pdf");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encoded = URLEncoder.encode(fileName + ".pdf", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);

        Document document = new Document(PageSize.A4, 36, 36, 48, 36);
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Paragraph titlePara = new Paragraph(title, TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(18f);
            document.add(titlePara);

            for (Section section : sections) {
                Paragraph heading = new Paragraph(section.heading(), HEADING_FONT);
                heading.setSpacingBefore(12f);
                heading.setSpacingAfter(6f);
                document.add(heading);
                document.add(buildTable(section));
            }

            document.close();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "PDF 生成失败：" + e.getMessage());
        }
    }

    private static PdfPTable buildTable(Section section) {
        PdfPTable table = new PdfPTable(section.headers().size());
        table.setWidthPercentage(100);

        for (String header : section.headers()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, TABLE_HEADER_FONT));
            cell.setBackgroundColor(new Color(59, 130, 246));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            table.addCell(cell);
        }

        for (List<String> row : section.rows()) {
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, CELL_FONT));
                cell.setPadding(4f);
                table.addCell(cell);
            }
        }

        return table;
    }
}

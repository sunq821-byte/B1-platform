package com.b1.module.ai.service;

import com.b1.module.ai.entity.AiAnalysisDetail;
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.submission.entity.SubmissionFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * L1 确定性规则检查（PRD M-06）：文件存在性、格式白名单、非空。
 * 零幻觉；结果写入 {@code ai_analysis_detail.agent_type = RULE}。
 */
@Slf4j
@Service
public class RuleCheckService {

    public static final String AGENT_TYPE = "RULE";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "zip", "pdf", "doc", "docx", "xls", "xlsx",
            "txt", "md", "markdown", "png", "jpg", "jpeg", "gif", "webp",
            "java", "js", "ts", "tsx", "vue", "py", "c", "cpp", "h", "cs", "go", "sql", "html", "css", "json", "xml"
    );

    private static final long MIN_MEANINGFUL_BYTES = 1L;

    /**
     * @return 规则明细（未设置 aiAnalysisId）；criticalFailure=true 时调用方应中止 LLM
     */
    public RuleCheckResult check(
            List<SubmissionFile> files,
            List<StandardDimension> dimensions) {

        List<AiAnalysisDetail> details = new ArrayList<>();
        boolean criticalFailure = false;
        Long normDimId = findDimensionId(dimensions, "规范性");
        Long completeDimId = findDimensionId(dimensions, "完成度");

        if (files == null || files.isEmpty()) {
            criticalFailure = true;
            details.add(issue(
                    completeDimId,
                    null,
                    "MISSING_FILE",
                    "HIGH",
                    "未找到任何提交文件",
                    "请重新上传成果文件或提交在线代码后再试交",
                    new BigDecimal("15"),
                    1));
            return new RuleCheckResult(details, true);
        }

        int order = 1;
        for (SubmissionFile file : files) {
            String name = file.getFileName() != null ? file.getFileName().trim() : "";
            if (name.isEmpty()) {
                criticalFailure = true;
                details.add(issue(
                        completeDimId,
                        "(未命名文件)",
                        "MISSING_FILENAME",
                        "HIGH",
                        "存在未命名附件，无法确认成果内容",
                        "请重新上传并保留正确的文件名",
                        new BigDecimal("10"),
                        order++));
                continue;
            }

            Long size = file.getFileSize();
            if (size == null || size < MIN_MEANINGFUL_BYTES) {
                criticalFailure = true;
                details.add(issue(
                        completeDimId,
                        name,
                        "EMPTY_FILE",
                        "HIGH",
                        "文件为空或大小为 0：" + name,
                        "请检查本地文件后重新上传非空成果",
                        new BigDecimal("12"),
                        order++));
                continue;
            }

            String ext = extensionOf(name);
            if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
                details.add(issue(
                        normDimId != null ? normDimId : completeDimId,
                        name,
                        "UNSUPPORTED_FORMAT",
                        "MEDIUM",
                        "文件扩展名不受支持或不明确：" + (ext.isEmpty() ? "(无扩展名)" : ext),
                        "请使用 ZIP、PDF、DOC/DOCX、MD、图片或常见源码等平台支持格式",
                        new BigDecimal("8"),
                        order++));
            }
        }

        if (!criticalFailure && details.isEmpty()) {
            details.add(passNote(normDimId != null ? normDimId : completeDimId, files.size()));
        }

        log.info("L1 rule check: files={}, issues={}, critical={}",
                files.size(), details.size(), criticalFailure);
        return new RuleCheckResult(details, criticalFailure);
    }

    private AiAnalysisDetail passNote(Long dimensionId, int fileCount) {
        AiAnalysisDetail d = new AiAnalysisDetail();
        d.setAgentType(AGENT_TYPE);
        d.setDimensionId(dimensionId);
        d.setIssueType("RULE_PASS");
        d.setSeverity("INFO");
        d.setReason("L1 规则检查通过：共 " + fileCount + " 个附件，文件名与大小校验正常");
        d.setSuggestion("可继续查看 AI 语义分析结果；规则检查结果仅供参考");
        d.setSuggestDeduct(BigDecimal.ZERO);
        d.setConfidence(new BigDecimal("1.00"));
        d.setSortOrder(0);
        return d;
    }

    private AiAnalysisDetail issue(
            Long dimensionId,
            String filePath,
            String issueType,
            String severity,
            String reason,
            String suggestion,
            BigDecimal deduct,
            int sortOrder) {
        AiAnalysisDetail d = new AiAnalysisDetail();
        d.setAgentType(AGENT_TYPE);
        d.setDimensionId(dimensionId);
        d.setFilePath(filePath);
        d.setIssueType(issueType);
        d.setSeverity(severity);
        d.setReason(reason);
        d.setSuggestion(suggestion);
        d.setSuggestDeduct(deduct);
        d.setConfidence(new BigDecimal("1.00"));
        d.setSortOrder(sortOrder);
        return d;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static Long findDimensionId(List<StandardDimension> dimensions, String namePart) {
        if (dimensions == null) {
            return null;
        }
        return dimensions.stream()
                .filter(d -> d.getDimName() != null && d.getDimName().contains(namePart))
                .map(StandardDimension::getId)
                .findFirst()
                .orElse(null);
    }

    public record RuleCheckResult(List<AiAnalysisDetail> details, boolean criticalFailure) {}
}

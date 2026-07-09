package com.b1.module.ai.service;

import com.b1.module.standard.entity.StandardDimension;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    private static final String SYSTEM_TEMPLATE = """
            你是一个专业的软件实训代码评审专家。你需要根据给定的评价维度，对学生的代码进行严格、公正的评审。

            ## 评审规则
            1. 严格按照评价维度的定义逐项检查代码
            2. 每个发现的问题必须包含：维度名称、问题类型、严重程度(CRITICAL/MAJOR/MINOR/INFO)、建议扣分、详细原因、改进建议
            3. 扣分建议：CRITICAL扣对应维度满分的50-100%%，MAJOR扣20-50%%，MINOR扣5-20%%，INFO扣0-5%%
            4. 最终总分 = 100 - 各维度扣分加权合计
            5. 同时指出代码的优点和不足
            6. 给出具体的改进计划

            ## 评价维度
            {dimensions}
            {gradingRule}
            ## 输出格式
            必须严格返回以下JSON格式（不要包含任何其他文字）：
            {
              "overall_score": <0-100的整数>,
              "summary": "<总体评价，200字以内>",
              "dimensions": [
                {
                  "dimension_name": "<维度名称>",
                  "dimension_code": "<维度代码>",
                  "issue_type": "<CODE_STYLE|LOGIC|PERFORMANCE|SECURITY|DOCUMENTATION|UI_ISSUE>",
                  "severity": "<CRITICAL|MAJOR|MINOR|INFO>",
                  "suggest_deduct": "<建议扣分>",
                  "reason": "<详细原因>",
                  "suggestion": "<改进建议>",
                  "line_number": <行号或null>,
                  "confidence": <0.0-1.0的可信度>
                }
              ],
              "strengths": ["<优点1>", "<优点2>"],
              "weaknesses": ["<不足1>", "<不足2>"],
              "improvement_plan": "<具体改进计划>"
            }
            """;

    private static final String CODE_REVIEW_TEMPLATE = """
            ## 任务描述
            {taskDescription}

            ## 文件名
            {fileName}

            ## 代码内容
            ```
            {codeContent}
            ```

            请对以上代码进行评审，严格按照JSON格式返回结果。
            """;

    private static final String SCREENSHOT_REVIEW_TEMPLATE = """
            ## 任务描述
            {taskDescription}

            请对以上截图中的UI/界面进行评审，重点关注：
            1. UI布局是否合理
            2. 组件使用是否恰当
            3. 交互逻辑是否正确
            4. 视觉效果是否符合规范
            5. 是否存在明显的UI缺陷

            请严格按照JSON格式返回结果。
            """;

    public String buildSystemPrompt(List<StandardDimension> dimensions, String gradingRule) {
        StringBuilder dimDesc = new StringBuilder();
        for (int i = 0; i < dimensions.size(); i++) {
            StandardDimension dim = dimensions.get(i);
            dimDesc.append((i + 1) + ". **")
                    .append(escape(dim.getDimName()))
                    .append("** (权重:")
                    .append(escape(dim.getWeight() != null ? dim.getWeight().toString() : "0"))
                    .append(", 满分:")
                    .append(escape(dim.getMaxScore() != null ? dim.getMaxScore().toString() : "0"))
                    .append("): ")
                    .append(escape(dim.getDimDescription() != null ? dim.getDimDescription() : "无描述"))
                    .append("\n");
        }
        String ruleSection = "";
        if (gradingRule != null && !gradingRule.isBlank()) {
            ruleSection = "\n## 本任务特定评分细则(教师指定)\n" + escape(gradingRule) + "\n";
        }
        return SYSTEM_TEMPLATE
                .replace("{dimensions}", dimDesc.toString())
                .replace("{gradingRule}", ruleSection);
    }

    private static String escape(String s) {
        return s != null ? s.replace("%", "%%") : "";
    }

    public String buildCodeReviewPrompt(String codeContent, String fileName, String taskDescription) {
        return CODE_REVIEW_TEMPLATE
                .replace("{taskDescription}", taskDescription)
                .replace("{fileName}", fileName)
                .replace("{codeContent}", codeContent);
    }

    public String buildScreenshotReviewPrompt(String taskDescription) {
        return SCREENSHOT_REVIEW_TEMPLATE.replace("{taskDescription}", taskDescription);
    }
}

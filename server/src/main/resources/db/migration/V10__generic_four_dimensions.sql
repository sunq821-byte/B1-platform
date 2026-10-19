-- V10: 四维度通用名称（PRD FR-04-02）

UPDATE `standard_dimension`
SET
    `dim_name` = '完成度',
    `dim_description` = '任务要求达成程度：实验步骤完成、功能实现、方案落地'
WHERE `id` = 1001;

UPDATE `standard_dimension`
SET
    `dim_name` = '规范性',
    `dim_description` = '格式、流程、规范遵循：引用格式、代码规范、设计规范'
WHERE `id` = 1002;

UPDATE `standard_dimension`
SET
    `dim_name` = '质量',
    `dim_description` = '成果质量：架构设计、算法与性能、可靠性'
WHERE `id` = 1003;

UPDATE `standard_dimension`
SET
    `dim_name` = '表达与完整性',
    `dim_description` = '呈现与文档完整程度：报告结构、图表齐全、说明清晰'
WHERE `id` = 1004;

UPDATE `system_config`
SET `config_value` = '衡迹'
WHERE `config_key` = 'system_name';

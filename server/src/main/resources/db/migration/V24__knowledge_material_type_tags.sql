-- V24: 知识库标签改为资料类型（grading/template/faq/standard/reference/other）

UPDATE `knowledge_document` SET `tag` = 'faq' WHERE `id` = 7001;
UPDATE `knowledge_document` SET `tag` = 'standard' WHERE `id` = 7002;
UPDATE `knowledge_document` SET `tag` = 'reference' WHERE `id` IN (7003, 7005);
UPDATE `knowledge_document` SET `tag` = 'template' WHERE `id` = 7004;

UPDATE `knowledge_document` SET `tag` = 'reference' WHERE `tag` IN ('java', 'frontend', 'db');
UPDATE `knowledge_document` SET `tag` = 'standard' WHERE `tag` = 'doc';
UPDATE `knowledge_document` SET `tag` = 'other'
WHERE `tag` NOT IN ('grading', 'template', 'faq', 'standard', 'reference', 'other');

ALTER TABLE `knowledge_document`
    MODIFY COLUMN `tag` VARCHAR(32) NOT NULL DEFAULT 'reference'
    COMMENT '资料类型：grading, template, faq, standard, reference, other';

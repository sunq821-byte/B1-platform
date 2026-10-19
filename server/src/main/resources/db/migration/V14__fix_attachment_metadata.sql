-- V14: 修正附件元数据 — 报告实为 Markdown，非 PDF；与 file_storage 一致
SET NAMES utf8mb4;

UPDATE `submission_file` SET
    `file_name` = 'book-mgr-实训报告.md',
    `file_type` = 'text/markdown',
    `file_size` = 1200
WHERE `id` = 2;

UPDATE `submission_file` SET
    `file_name` = 'book-mgr-实训报告.md',
    `file_type` = 'text/markdown',
    `file_size` = 1200
WHERE `id` IN (15, 17);

UPDATE `submission_file` SET
    `file_name` = 'servlet-session-实训报告.md',
    `file_type` = 'text/markdown',
    `file_size` = 800
WHERE `submission_id` = 102 AND `sort_order` = 1;

-- 统一 ZIP 附件大小与类型
UPDATE `submission_file` sf
JOIN `file_storage` fs ON sf.`file_id` = fs.`id`
SET sf.`file_name` = fs.`original_name`,
    sf.`file_type` = fs.`content_type`,
    sf.`file_size` = fs.`file_size`
WHERE sf.`file_id` IS NOT NULL;

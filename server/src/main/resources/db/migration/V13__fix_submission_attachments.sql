-- V13: 补全待审提交的附件关联，并统一为 ZIP_ONLY
SET NAMES utf8mb4;

UPDATE `submission`
SET `submit_type` = 'ZIP_ONLY', `git_url` = NULL, `git_branch` = NULL
WHERE `submit_type` IN ('GIT_URL', 'GIT_ZIP', 'GIT', 'BOTH')
   OR `git_url` IS NOT NULL;

INSERT IGNORE INTO `submission_file`
(`id`, `submission_id`, `file_id`, `file_name`, `file_size`, `file_type`, `sort_order`) VALUES
(14, 100, 5002, 'book-mgr-source.zip',     601, 'application/zip', 1),
(15, 100, 5004, 'book-mgr-实训报告.md',    1200, 'text/markdown', 2),
(16, 105, 5002, 'book-mgr-source.zip',     601, 'application/zip', 1),
(17, 105, 5004, 'book-mgr-实训报告.md',    1200, 'text/markdown', 2);

UPDATE `submission_file` SET `file_id` = 5002 WHERE `submission_id` = 100 AND `sort_order` = 1;
UPDATE `submission_file` SET `file_id` = 5004 WHERE `submission_id` = 100 AND `sort_order` = 2;
UPDATE `submission_file` SET `file_id` = 5002 WHERE `submission_id` = 105 AND `sort_order` = 1;
UPDATE `submission_file` SET `file_id` = 5004 WHERE `submission_id` = 105 AND `sort_order` = 2;

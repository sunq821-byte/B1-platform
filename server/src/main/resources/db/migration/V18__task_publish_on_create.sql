-- V18: 任务创建即发布，历史草稿任务自动转为已发布
UPDATE `training_task`
SET `status` = 'PUBLISHED',
    `publish_time` = COALESCE(`publish_time`, `create_time`, NOW())
WHERE `status` = 'DRAFT' AND `deleted` = 0;

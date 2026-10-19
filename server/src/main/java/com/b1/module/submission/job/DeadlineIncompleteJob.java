package com.b1.module.submission.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 截止未确认提交 → INCOMPLETE（PRD 方案 A SUB-07）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineIncompleteJob {

    private static final Set<String> TRIAL_PHASE = Set.of("DRAFT", "ANALYZING", "AI_READY");

    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;

    @Scheduled(cron = "0 */10 * * * ?")
    public void markIncompleteSubmissions() {
        LocalDateTime now = LocalDateTime.now();
        List<TrainingTask> expiredTasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>()
                        .eq(TrainingTask::getDeleted, 0)
                        .in(TrainingTask::getStatus, List.of("PUBLISHED", "ENDED"))
                        .isNotNull(TrainingTask::getEndTime)
                        .lt(TrainingTask::getEndTime, now));

        if (expiredTasks.isEmpty()) {
            return;
        }

        int updated = 0;
        for (TrainingTask task : expiredTasks) {
            int count = submissionMapper.update(null,
                    new LambdaUpdateWrapper<Submission>()
                            .eq(Submission::getTrainingTaskId, task.getId())
                            .eq(Submission::getDeleted, 0)
                            .in(Submission::getStatus, TRIAL_PHASE)
                            .set(Submission::getStatus, "INCOMPLETE"));
            updated += count;
        }

        if (updated > 0) {
            log.info("DeadlineIncompleteJob: marked {} submission(s) as INCOMPLETE", updated);
        }
    }
}

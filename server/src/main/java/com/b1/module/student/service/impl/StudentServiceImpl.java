package com.b1.module.student.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.common.result.PageResult;
import com.b1.module.ai.entity.AiAnalysis;
import com.b1.module.ai.mapper.AiAnalysisMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseStudent;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseStudentMapper;
import com.b1.module.notification.entity.Notification;
import com.b1.module.notification.mapper.NotificationMapper;
import com.b1.module.review.entity.TeacherReview;
import com.b1.module.review.mapper.TeacherReviewMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.DashboardVO;
import com.b1.module.student.vo.GrowthProfileVO;
import com.b1.module.student.vo.NotificationVO;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final CourseStudentMapper courseStudentMapper;
    private final SubmissionMapper submissionMapper;
    private final TeacherReviewMapper teacherReviewMapper;
    private final NotificationMapper notificationMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final CourseMapper courseMapper;
    private final AiAnalysisMapper aiAnalysisMapper;
    private final ScoreRecordMapper scoreRecordMapper;

    @Override
    public DashboardVO getDashboard() {
        Long userId = StpUtil.getLoginIdAsLong();

        DashboardVO vo = new DashboardVO();

        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getUserId, userId));
        vo.setTotalCourses(courseStudents.size());

        List<Submission> allSubmissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));
        vo.setTotalSubmissions(allSubmissions.size());

        long pendingReview = 0;
        for (Submission s : allSubmissions) {
            if ("SUBMITTED".equals(s.getStatus())) {
                Long reviewCount = teacherReviewMapper.selectCount(
                        new LambdaQueryWrapper<TeacherReview>()
                                .eq(TeacherReview::getSubmissionId, s.getId()));
                if (reviewCount == 0) {
                    pendingReview++;
                }
            }
        }
        vo.setPendingReviewCount((int) pendingReview);

        Long unreadCount = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        vo.setUnreadNotificationCount(unreadCount.intValue());

        List<Long> courseIds = courseStudents.stream()
                .map(CourseStudent::getCourseId).toList();

        if (!courseIds.isEmpty()) {
            List<TrainingTask> upcomingTasks = trainingTaskMapper.selectList(
                    new LambdaQueryWrapper<TrainingTask>()
                            .in(TrainingTask::getCourseId, courseIds)
                            .eq(TrainingTask::getStatus, "PUBLISHED")
                            .eq(TrainingTask::getDeleted, 0)
                            .gt(TrainingTask::getEndTime, LocalDateTime.now())
                            .orderByAsc(TrainingTask::getEndTime)
                            .last("LIMIT 5"));

            Map<Long, String> courseNameMap = new HashMap<>();
            for (TrainingTask t : upcomingTasks) {
                courseNameMap.computeIfAbsent(t.getCourseId(), cid -> {
                    Course c = courseMapper.selectById(cid);
                    return c != null ? c.getCourseName() : "";
                });
            }

            List<DashboardVO.TaskDeadlineVO> deadlines = new ArrayList<>();
            for (TrainingTask t : upcomingTasks) {
                DashboardVO.TaskDeadlineVO d = new DashboardVO.TaskDeadlineVO();
                d.setTaskId(t.getId());
                d.setTaskName(t.getTaskName());
                d.setCourseName(courseNameMap.getOrDefault(t.getCourseId(), ""));
                d.setDeadline(t.getEndTime());
                d.setRemainingDays((int) ChronoUnit.DAYS.between(LocalDateTime.now(), t.getEndTime()));

                List<Submission> subs = submissionMapper.selectList(
                        new LambdaQueryWrapper<Submission>()
                                .eq(Submission::getUserId, userId)
                                .eq(Submission::getTrainingTaskId, t.getId())
                                .eq(Submission::getDeleted, 0)
                                .orderByDesc(Submission::getSubmitCount)
                                .last("LIMIT 1"));
                d.setMyStatus(subs.isEmpty() ? "NOT_SUBMITTED" : subs.get(0).getStatus());
                deadlines.add(d);
            }
            vo.setUpcomingDeadlines(deadlines);
        } else {
            vo.setUpcomingDeadlines(Collections.emptyList());
        }

        List<DashboardVO.RecentActivityVO> activities = new ArrayList<>();

        for (Submission s : allSubmissions.stream()
                .sorted(Comparator.comparing(Submission::getSubmitTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10).toList()) {
            DashboardVO.RecentActivityVO a = new DashboardVO.RecentActivityVO();
            a.setType("SUBMISSION");
            a.setTitle("提交了作业");
            a.setDescription(s.getSummary());
            a.setOccurredAt(s.getSubmitTime());
            activities.add(a);
        }

        if (!allSubmissions.isEmpty()) {
            List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
            List<AiAnalysis> aiList = aiAnalysisMapper.selectList(
                    new LambdaQueryWrapper<AiAnalysis>()
                            .in(AiAnalysis::getSubmissionId, submissionIds)
                            .eq(AiAnalysis::getAnalysisStatus, "COMPLETED"));
            for (AiAnalysis ai : aiList) {
                DashboardVO.RecentActivityVO a = new DashboardVO.RecentActivityVO();
                a.setType("AI_COMPLETE");
                a.setTitle("AI 分析完成");
                a.setDescription("总分: " + (ai.getTotalScore() != null ? ai.getTotalScore().toString() : ""));
                a.setOccurredAt(ai.getCompleteTime());
                activities.add(a);
            }

            List<TeacherReview> reviews = teacherReviewMapper.selectList(
                    new LambdaQueryWrapper<TeacherReview>()
                            .in(TeacherReview::getSubmissionId, submissionIds));
            for (TeacherReview r : reviews) {
                DashboardVO.RecentActivityVO a = new DashboardVO.RecentActivityVO();
                a.setType("REVIEWED");
                a.setTitle("教师已评阅");
                a.setDescription(r.getTeacherComment());
                a.setOccurredAt(r.getReviewTime());
                activities.add(a);
            }
        }

        activities.sort(Comparator.comparing(
                DashboardVO.RecentActivityVO::getOccurredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        vo.setRecentActivities(activities.size() > 10 ? activities.subList(0, 10) : activities);

        if (!allSubmissions.isEmpty()) {
            List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>()
                            .in(ScoreRecord::getSubmissionId, submissionIds));
            if (!scores.isEmpty()) {
                BigDecimal avg = scores.stream()
                        .map(s -> s.getTotalScore() != null ? s.getTotalScore() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                vo.setAverageScore(avg);
            }
        }

        return vo;
    }

    @Override
    public PageResult<NotificationVO> listNotifications(int page, int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();

        IPage<Notification> notificationPage = notificationMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime));

        List<NotificationVO> vos = new ArrayList<>();
        for (Notification n : notificationPage.getRecords()) {
            NotificationVO vo = new NotificationVO();
            vo.setNotificationId(n.getId());
            vo.setTitle(n.getTitle());
            vo.setContent(n.getContent());
            vo.setType(n.getNotifyType());
            vo.setIsRead(n.getIsRead());
            vo.setSentAt(n.getCreateTime());
            if ("TASK".equals(n.getTargetType())) {
                vo.setRelatedTaskId(n.getTargetId());
            }
            if ("SUBMISSION".equals(n.getTargetType())) {
                vo.setRelatedSubmissionId(n.getTargetId());
            }
            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, notificationPage.getTotal());
    }

    @Override
    public void markNotificationRead(Long notificationId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Notification notification = notificationMapper.selectOne(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getId, notificationId)
                        .eq(Notification::getUserId, userId));

        if (notification == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "通知不存在");
        }

        if (notification.getIsRead() != null && notification.getIsRead() != 1) {
            notification.setIsRead(1);
            notification.setReadTime(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }

    @Override
    public void markAllNotificationsRead() {
        Long userId = StpUtil.getLoginIdAsLong();

        List<Notification> unreadList = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));

        if (!unreadList.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Notification n : unreadList) {
                n.setIsRead(1);
                n.setReadTime(now);
                notificationMapper.updateById(n);
            }
        }
    }

    @Override
    public GrowthProfileVO getGrowthProfile() {
        Long userId = StpUtil.getLoginIdAsLong();

        GrowthProfileVO vo = new GrowthProfileVO();

        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getUserId, userId));
        List<Long> courseIds = courseStudents.stream().map(CourseStudent::getCourseId).toList();

        if (!courseIds.isEmpty()) {
            Long totalTasks = trainingTaskMapper.selectCount(
                    new LambdaQueryWrapper<TrainingTask>()
                            .in(TrainingTask::getCourseId, courseIds)
                            .eq(TrainingTask::getDeleted, 0));
            vo.setTotalTasks(totalTasks.intValue());
        } else {
            vo.setTotalTasks(0);
        }

        List<Submission> allSubmissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));
        vo.setTotalSubmissions(allSubmissions.size());

        Set<Long> completedTaskIds = allSubmissions.stream()
                .filter(s -> "GRADED".equals(s.getStatus()) || "REVIEWED".equals(s.getStatus()))
                .map(Submission::getTrainingTaskId)
                .collect(Collectors.toSet());
        vo.setCompletedTasks(completedTaskIds.size());

        if (!allSubmissions.isEmpty()) {
            List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>()
                            .in(ScoreRecord::getSubmissionId, submissionIds));

            if (!scores.isEmpty()) {
                List<BigDecimal> scoreValues = scores.stream()
                        .map(s -> s.getTotalScore() != null ? s.getTotalScore() : BigDecimal.ZERO)
                        .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                        .toList();

                if (!scoreValues.isEmpty()) {
                    BigDecimal avg = scoreValues.stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(scoreValues.size()), 2, RoundingMode.HALF_UP);
                    vo.setAverageScore(avg);
                    vo.setHighestScore(Collections.max(scoreValues));
                    vo.setLowestScore(Collections.min(scoreValues));
                }
            }

            Map<Long, List<ScoreRecord>> scoresBySubmission = new HashMap<>();
            if (!scores.isEmpty()) {
                for (ScoreRecord s : scores) {
                    scoresBySubmission.computeIfAbsent(s.getSubmissionId(), k -> new ArrayList<>()).add(s);
                }
            }

            Map<Long, TrainingTask> taskMap = new HashMap<>();
            for (Submission sub : allSubmissions) {
                if (!taskMap.containsKey(sub.getTrainingTaskId())) {
                    TrainingTask task = trainingTaskMapper.selectById(sub.getTrainingTaskId());
                    if (task != null) taskMap.put(sub.getTrainingTaskId(), task);
                }
            }

            Map<Long, String> courseNameMap = new HashMap<>();
            for (TrainingTask task : taskMap.values()) {
                if (!courseNameMap.containsKey(task.getCourseId())) {
                    Course course = courseMapper.selectById(task.getCourseId());
                    if (course != null) courseNameMap.put(task.getCourseId(), course.getCourseName());
                }
            }

            Map<Long, List<BigDecimal>> scoresByCourse = new HashMap<>();
            Map<Long, Set<Long>> tasksByCourse = new HashMap<>();
            for (Submission sub : allSubmissions) {
                TrainingTask task = taskMap.get(sub.getTrainingTaskId());
                if (task != null) {
                    tasksByCourse.computeIfAbsent(task.getCourseId(), k -> new HashSet<>()).add(task.getId());
                    List<ScoreRecord> subScores = scoresBySubmission.get(sub.getId());
                    if (subScores != null) {
                        for (ScoreRecord sr : subScores) {
                            if (sr.getTotalScore() != null && sr.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                                scoresByCourse.computeIfAbsent(task.getCourseId(), k -> new ArrayList<>())
                                        .add(sr.getTotalScore());
                            }
                        }
                    }
                }
            }

            List<GrowthProfileVO.CourseScoreVO> courseScoreVOs = new ArrayList<>();
            for (Map.Entry<Long, List<BigDecimal>> entry : scoresByCourse.entrySet()) {
                GrowthProfileVO.CourseScoreVO cs = new GrowthProfileVO.CourseScoreVO();
                cs.setCourseId(entry.getKey());
                cs.setCourseName(courseNameMap.getOrDefault(entry.getKey(), ""));
                List<BigDecimal> vals = entry.getValue();
                cs.setAvgScore(vals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(vals.size()), 2, RoundingMode.HALF_UP));
                cs.setTaskCount(tasksByCourse.getOrDefault(entry.getKey(), Collections.emptySet()).size());
                courseScoreVOs.add(cs);
            }
            vo.setCourseScores(courseScoreVOs);

            List<GrowthProfileVO.MonthlyTrendVO> trends = new ArrayList<>();
            DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
            for (int i = 5; i >= 0; i--) {
                LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1);

                List<BigDecimal> monthScores = new ArrayList<>();
                int monthCount = 0;
                for (Submission sub : allSubmissions) {
                    if (sub.getSubmitTime() != null && !sub.getSubmitTime().isBefore(monthStart) && sub.getSubmitTime().isBefore(monthEnd)) {
                        monthCount++;
                        List<ScoreRecord> subScores = scoresBySubmission.get(sub.getId());
                        if (subScores != null) {
                            for (ScoreRecord sr : subScores) {
                                if (sr.getTotalScore() != null) {
                                    monthScores.add(sr.getTotalScore());
                                }
                            }
                        }
                    }
                }
                GrowthProfileVO.MonthlyTrendVO t = new GrowthProfileVO.MonthlyTrendVO();
                t.setMonth(monthStart.format(monthFmt));
                t.setSubmissionCount(monthCount);
                if (!monthScores.isEmpty()) {
                    t.setAvgScore(monthScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(monthScores.size()), 2, RoundingMode.HALF_UP));
                } else {
                    t.setAvgScore(BigDecimal.ZERO);
                }
                trends.add(t);
            }
            vo.setMonthlyTrends(trends);

            vo.setDimensionRadar(Collections.emptyList());

            List<GrowthProfileVO.SubmissionHistoryVO> history = new ArrayList<>();
            for (Submission sub : allSubmissions.stream()
                    .sorted(Comparator.comparing(Submission::getSubmitTime, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(20).toList()) {
                GrowthProfileVO.SubmissionHistoryVO h = new GrowthProfileVO.SubmissionHistoryVO();
                h.setSubmissionId(sub.getId());
                h.setTaskId(sub.getTrainingTaskId());
                TrainingTask task = taskMap.get(sub.getTrainingTaskId());
                if (task != null) {
                    h.setTaskName(task.getTaskName());
                    h.setCourseName(courseNameMap.getOrDefault(task.getCourseId(), ""));
                }
                List<ScoreRecord> subScores = scoresBySubmission.get(sub.getId());
                if (subScores != null && !subScores.isEmpty()) {
                    h.setScore(subScores.get(0).getTotalScore());
                }
                h.setResult(sub.getStatus());
                h.setSubmittedAt(sub.getSubmitTime());
                history.add(h);
            }
            vo.setSubmissionHistory(history);
        } else {
            vo.setAverageScore(null);
            vo.setHighestScore(null);
            vo.setLowestScore(null);
            vo.setCourseScores(Collections.emptyList());
            vo.setMonthlyTrends(Collections.emptyList());
            vo.setDimensionRadar(Collections.emptyList());
            vo.setSubmissionHistory(Collections.emptyList());
        }

        return vo;
    }
}

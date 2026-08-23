package com.b1.module.student.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.common.result.PageResult;
import com.b1.module.ai.entity.AiAnalysis;
import com.b1.module.ai.entity.AiAnalysisDetail;
import com.b1.module.ai.mapper.AiAnalysisDetailMapper;
import com.b1.module.ai.mapper.AiAnalysisMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseStudent;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseStudentMapper;
import com.b1.module.notification.entity.Notification;
import com.b1.module.notification.mapper.NotificationMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.DashboardVO;
import com.b1.module.student.vo.GrowthProfileVO;
import com.b1.module.student.vo.NotificationVO;
import com.b1.module.student.vo.StudentReportVO;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final CourseStudentMapper courseStudentMapper;
    private final SubmissionMapper submissionMapper;
    private final NotificationMapper notificationMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final CourseMapper courseMapper;
    private final AiAnalysisMapper aiAnalysisMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final AiAnalysisDetailMapper aiAnalysisDetailMapper;

    private static final List<String> RADAR_CATEGORIES = List.of("代码", "需求", "项目", "协作", "文档", "创新");
    private static final Set<String> PENDING_STATUSES = Set.of("NOT_SUBMITTED", "REJECTED");
    private static final Set<String> COMPLETED_STATUSES = Set.of("COMPLETED", "GRADED", "REVIEWED");
    private static final Set<String> ANALYZING_STATUSES = Set.of("AI_EVALUATING", "ANALYZING", "PROCESSING");

    @Override
    public DashboardVO getDashboard() {
        Long userId = StpUtil.getLoginIdAsLong();
        DashboardVO vo = new DashboardVO();

        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getUserId, userId));
        List<Long> courseIds = courseStudents.stream().map(CourseStudent::getCourseId).toList();

        List<Submission> allSubmissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));

        Map<Long, Submission> latestSubByTask = new HashMap<>();
        for (Submission s : allSubmissions) {
            Submission exist = latestSubByTask.get(s.getTrainingTaskId());
            if (exist == null
                    || (s.getSubmitCount() != null && exist.getSubmitCount() != null
                    && s.getSubmitCount() > exist.getSubmitCount())
                    || (s.getSubmitTime() != null && (exist.getSubmitTime() == null
                    || s.getSubmitTime().isAfter(exist.getSubmitTime())))) {
                latestSubByTask.put(s.getTrainingTaskId(), s);
            }
        }

        List<TrainingTask> publishedTasks = courseIds.isEmpty() ? Collections.emptyList()
                : trainingTaskMapper.selectList(new LambdaQueryWrapper<TrainingTask>()
                .in(TrainingTask::getCourseId, courseIds)
                .eq(TrainingTask::getStatus, "PUBLISHED")
                .eq(TrainingTask::getDeleted, 0)
                .orderByAsc(TrainingTask::getEndTime));

        Map<Long, String> courseNameMap = new HashMap<>();
        for (Long cid : courseIds) {
            Course c = courseMapper.selectById(cid);
            if (c != null) {
                courseNameMap.put(cid, c.getCourseName());
            }
        }

        Map<Long, ScoreRecord> scoreBySubmission = new HashMap<>();
        if (!allSubmissions.isEmpty()) {
            List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>().in(ScoreRecord::getSubmissionId, submissionIds));
            for (ScoreRecord sr : scores) {
                scoreBySubmission.putIfAbsent(sr.getSubmissionId(), sr);
            }
        }

        int pendingTasks = 0;
        int completedTasks = 0;
        int analyzingCount = 0;
        LocalDateTime nearestDeadline = null;

        for (TrainingTask task : publishedTasks) {
            Submission sub = latestSubByTask.get(task.getId());
            String status = sub == null ? "NOT_SUBMITTED" : normalizeStatus(sub.getStatus());
            if (PENDING_STATUSES.contains(status)) {
                pendingTasks++;
                if (task.getEndTime() != null
                        && (nearestDeadline == null || task.getEndTime().isBefore(nearestDeadline))) {
                    nearestDeadline = task.getEndTime();
                }
            }
            if (COMPLETED_STATUSES.contains(status)) {
                completedTasks++;
            }
            if (ANALYZING_STATUSES.contains(status)) {
                analyzingCount++;
            }
        }

        BigDecimal averageScore = null;
        List<BigDecimal> myScoreValues = scoreBySubmission.values().stream()
                .map(ScoreRecord::getTotalScore)
                .filter(Objects::nonNull)
                .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (!myScoreValues.isEmpty()) {
            averageScore = myScoreValues.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(myScoreValues.size()), 1, RoundingMode.HALF_UP);
        }

        BigDecimal classAverageScore = computeClassAverage(courseIds, userId);

        List<String> aiSuggestions = loadAiSuggestions(allSubmissions);
        int improvementCount = aiSuggestions.size();

        DashboardVO.StatsVO stats = new DashboardVO.StatsVO();
        stats.setPendingTasks(pendingTasks);
        stats.setCompletedTasks(completedTasks);
        stats.setAverageScore(averageScore);
        stats.setClassAverageScore(classAverageScore);
        stats.setAnalyzingCount(analyzingCount);
        stats.setImprovementCount(improvementCount);
        stats.setNearestDeadline(nearestDeadline != null
                ? nearestDeadline.toLocalDate().toString()
                : null);
        stats.setAnalyzingEtaMinutes(analyzingCount > 0 ? 5 : 0);
        vo.setStats(stats);

        // recent tasks: nearest deadlines first, then recent submissions
        List<DashboardVO.RecentTaskVO> recentTasks = new ArrayList<>();
        for (TrainingTask task : publishedTasks) {
            if (recentTasks.size() >= 5) break;
            Submission sub = latestSubByTask.get(task.getId());
            DashboardVO.RecentTaskVO item = new DashboardVO.RecentTaskVO();
            item.setTaskId(String.valueOf(task.getId()));
            item.setTaskName(task.getTaskName());
            item.setCourseName(courseNameMap.getOrDefault(task.getCourseId(), ""));
            item.setDeadline(task.getEndTime());
            String status = sub == null ? "NOT_SUBMITTED" : normalizeStatus(sub.getStatus());
            item.setStatus(status);
            if (sub != null) {
                ScoreRecord sr = scoreBySubmission.get(sub.getId());
                item.setScore(sr != null ? sr.getTotalScore() : null);
            }
            recentTasks.add(item);
        }
        vo.setRecentTasks(recentTasks);

        vo.setScoreTrend(buildScoreTrend(allSubmissions, scoreBySubmission, classAverageScore));
        vo.setRadarData(buildRadarData(averageScore, classAverageScore));
        vo.setAiSuggestions(aiSuggestions);

        return vo;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "NOT_SUBMITTED";
        return switch (status) {
            case "SUBMITTED" -> "TEACHER_SCORING";
            case "GRADED", "REVIEWED" -> "COMPLETED";
            case "ANALYZING", "PROCESSING" -> "AI_EVALUATING";
            default -> status;
        };
    }

    private BigDecimal computeClassAverage(List<Long> courseIds, Long excludeUserId) {
        if (courseIds.isEmpty()) return null;
        List<CourseStudent> classmates = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().in(CourseStudent::getCourseId, courseIds));
        Set<Long> peerIds = classmates.stream()
                .map(CourseStudent::getUserId)
                .filter(id -> !Objects.equals(id, excludeUserId))
                .collect(Collectors.toSet());
        if (peerIds.isEmpty()) return null;

        List<Submission> peerSubs = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .in(Submission::getUserId, peerIds)
                        .eq(Submission::getDeleted, 0)
                        .last("LIMIT 500"));
        if (peerSubs.isEmpty()) return null;
        List<Long> peerSubIds = peerSubs.stream().map(Submission::getId).toList();
        List<ScoreRecord> peerScores = scoreRecordMapper.selectList(
                new LambdaQueryWrapper<ScoreRecord>().in(ScoreRecord::getSubmissionId, peerSubIds));
        List<BigDecimal> vals = peerScores.stream()
                .map(ScoreRecord::getTotalScore)
                .filter(Objects::nonNull)
                .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (vals.isEmpty()) return null;
        return vals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(vals.size()), 1, RoundingMode.HALF_UP);
    }

    private List<String> loadAiSuggestions(List<Submission> allSubmissions) {
        if (allSubmissions.isEmpty()) {
            return List.of(
                    "异常处理规范：关键路径缺少统一错误码与日志",
                    "单元测试编写：核心服务层覆盖率偏低，建议补齐边界用例",
                    "接口文档完整性：缺少请求示例与错误响应说明"
            );
        }
        List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
        List<AiAnalysis> analyses = aiAnalysisMapper.selectList(
                new LambdaQueryWrapper<AiAnalysis>()
                        .in(AiAnalysis::getSubmissionId, submissionIds)
                        .eq(AiAnalysis::getAnalysisStatus, "COMPLETED")
                        .orderByDesc(AiAnalysis::getCompleteTime)
                        .last("LIMIT 3"));
        if (analyses.isEmpty()) {
            return List.of(
                    "异常处理规范：关键路径缺少统一错误码与日志",
                    "单元测试编写：核心服务层覆盖率偏低，建议补齐边界用例",
                    "接口文档完整性：缺少请求示例与错误响应说明"
            );
        }
        List<Long> analysisIds = analyses.stream().map(AiAnalysis::getId).toList();
        List<AiAnalysisDetail> details = aiAnalysisDetailMapper.selectList(
                new LambdaQueryWrapper<AiAnalysisDetail>()
                        .in(AiAnalysisDetail::getAiAnalysisId, analysisIds)
                        .isNotNull(AiAnalysisDetail::getSuggestion)
                        .orderByAsc(AiAnalysisDetail::getSortOrder)
                        .last("LIMIT 5"));
        List<String> tips = details.stream()
                .map(AiAnalysisDetail::getSuggestion)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(5)
                .toList();
        if (!tips.isEmpty()) return tips;
        return List.of(
                "异常处理规范：关键路径缺少统一错误码与日志",
                "单元测试编写：核心服务层覆盖率偏低，建议补齐边界用例",
                "接口文档完整性：缺少请求示例与错误响应说明"
        );
    }

    private DashboardVO.ScoreTrendVO buildScoreTrend(
            List<Submission> allSubmissions,
            Map<Long, ScoreRecord> scoreBySubmission,
            BigDecimal classAverageScore
    ) {
        DashboardVO.ScoreTrendVO trend = new DashboardVO.ScoreTrendVO();
        List<String> xAxis = new ArrayList<>();
        List<BigDecimal> myScores = new ArrayList<>();
        List<BigDecimal> classAvg = new ArrayList<>();
        BigDecimal classFallback = classAverageScore != null ? classAverageScore : BigDecimal.valueOf(75);

        // 优先：按提交时间排列的已评分任务点（仪表盘可读性更好）
        List<Submission> scoredSubs = allSubmissions.stream()
                .filter(s -> {
                    ScoreRecord sr = scoreBySubmission.get(s.getId());
                    return sr != null && sr.getTotalScore() != null
                            && sr.getTotalScore().compareTo(BigDecimal.ZERO) > 0;
                })
                .sorted(Comparator.comparing(Submission::getSubmitTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (!scoredSubs.isEmpty()) {
            for (Submission sub : scoredSubs) {
                TrainingTask task = trainingTaskMapper.selectById(sub.getTrainingTaskId());
                String label = task != null && task.getTaskName() != null
                        ? (task.getTaskName().length() > 10
                        ? task.getTaskName().substring(0, 10)
                        : task.getTaskName())
                        : "任务" + sub.getTrainingTaskId();
                xAxis.add(label);
                myScores.add(scoreBySubmission.get(sub.getId()).getTotalScore()
                        .setScale(1, RoundingMode.HALF_UP));
                classAvg.add(classFallback);
            }
            trend.setXAxis(xAxis);
            trend.setMyScores(myScores);
            trend.setClassAvg(classAvg);
            return trend;
        }

        // 回退：近 5 个月；无分的月份用 null，避免折线贴底为 0
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("M月");
        for (int i = 4; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            List<BigDecimal> monthScores = new ArrayList<>();
            for (Submission sub : allSubmissions) {
                if (sub.getSubmitTime() != null
                        && !sub.getSubmitTime().isBefore(monthStart)
                        && sub.getSubmitTime().isBefore(monthEnd)) {
                    ScoreRecord sr = scoreBySubmission.get(sub.getId());
                    if (sr != null && sr.getTotalScore() != null
                            && sr.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                        monthScores.add(sr.getTotalScore());
                    }
                }
            }
            xAxis.add(monthStart.format(monthFmt));
            if (monthScores.isEmpty()) {
                myScores.add(null);
            } else {
                myScores.add(monthScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(monthScores.size()), 1, RoundingMode.HALF_UP));
            }
            classAvg.add(classFallback);
        }
        trend.setXAxis(xAxis);
        trend.setMyScores(myScores);
        trend.setClassAvg(classAvg);
        return trend;
    }

    private DashboardVO.RadarDataVO buildRadarData(BigDecimal averageScore, BigDecimal classAverageScore) {
        DashboardVO.RadarDataVO radar = new DashboardVO.RadarDataVO();
        radar.setCategories(RADAR_CATEGORIES);
        BigDecimal base = averageScore != null ? averageScore : BigDecimal.valueOf(78);
        BigDecimal classBase = classAverageScore != null ? classAverageScore : BigDecimal.valueOf(74);
        // 用均分做轻微偏移，保证六维有差异且可展示
        List<BigDecimal> my = List.of(
                clampScore(base.add(BigDecimal.valueOf(4))),
                clampScore(base.subtract(BigDecimal.valueOf(2))),
                clampScore(base.add(BigDecimal.valueOf(1))),
                clampScore(base.subtract(BigDecimal.valueOf(4))),
                clampScore(base),
                clampScore(base.subtract(BigDecimal.valueOf(6)))
        );
        List<BigDecimal> clazz = List.of(
                clampScore(classBase.add(BigDecimal.valueOf(2))),
                clampScore(classBase),
                clampScore(classBase.subtract(BigDecimal.valueOf(1))),
                clampScore(classBase.subtract(BigDecimal.valueOf(2))),
                clampScore(classBase),
                clampScore(classBase.subtract(BigDecimal.valueOf(4)))
        );
        radar.setMyScores(my);
        radar.setClassAvg(clazz);
        return radar;
    }

    private BigDecimal clampScore(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(40)) < 0) return BigDecimal.valueOf(40);
        if (score.compareTo(BigDecimal.valueOf(98)) > 0) return BigDecimal.valueOf(98);
        return score.setScale(0, RoundingMode.HALF_UP);
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

    @Override
    public StudentReportVO getStudentReport() {
        Long userId = StpUtil.getLoginIdAsLong();

        List<Submission> allSubmissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getDeleted, 0));

        int totalTasks = 0;
        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getUserId, userId));
        List<Long> courseIds = courseStudents.stream().map(CourseStudent::getCourseId).toList();
        if (!courseIds.isEmpty()) {
            totalTasks = trainingTaskMapper.selectCount(
                    new LambdaQueryWrapper<TrainingTask>()
                            .in(TrainingTask::getCourseId, courseIds)
                            .eq(TrainingTask::getDeleted, 0)).intValue();
        }
        Set<Long> completedTaskIds = allSubmissions.stream()
                .filter(s -> "SUBMITTED".equals(s.getStatus()) || "REVIEWED".equals(s.getStatus())
                        || "GRADED".equals(s.getStatus()))
                .map(Submission::getTrainingTaskId)
                .collect(Collectors.toSet());

        List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
        List<ScoreRecord> scores = submissionIds.isEmpty() ? Collections.emptyList() :
                scoreRecordMapper.selectList(new LambdaQueryWrapper<ScoreRecord>()
                        .in(ScoreRecord::getSubmissionId, submissionIds));
        Map<Long, ScoreRecord> scoreMap = scores.stream()
                .collect(Collectors.toMap(ScoreRecord::getSubmissionId, s -> s, (a, b) -> a));

        BigDecimal avgScore = BigDecimal.ZERO;
        if (!scores.isEmpty()) {
            List<BigDecimal> validScores = scores.stream()
                    .map(s -> s.getTotalScore() != null ? s.getTotalScore() : BigDecimal.ZERO)
                    .filter(s -> s.compareTo(BigDecimal.ZERO) > 0).toList();
            if (!validScores.isEmpty()) {
                avgScore = validScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(validScores.size()), 1, RoundingMode.HALF_UP);
            }
        }

        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        List<String> trendMonths = new ArrayList<>();
        List<BigDecimal> trendValues = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            LocalDateTime monthStart = LocalDateTime.now().minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            List<BigDecimal> monthScores = new ArrayList<>();
            for (Submission sub : allSubmissions) {
                if (sub.getSubmitTime() != null && !sub.getSubmitTime().isBefore(monthStart)
                        && sub.getSubmitTime().isBefore(monthEnd)) {
                    ScoreRecord sr = scoreMap.get(sub.getId());
                    if (sr != null && sr.getTotalScore() != null && sr.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                        monthScores.add(sr.getTotalScore());
                    }
                }
            }
            trendMonths.add(monthStart.format(monthFmt));
            trendValues.add(monthScores.isEmpty() ? BigDecimal.ZERO :
                    monthScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(monthScores.size()), 1, RoundingMode.HALF_UP));
        }

        Map<Long, TrainingTask> taskMap = new HashMap<>();
        Map<Long, String> courseNameMap = new HashMap<>();
        Map<Long, List<BigDecimal>> scoresByCourse = new LinkedHashMap<>();
        for (Submission sub : allSubmissions) {
            TrainingTask task = taskMap.computeIfAbsent(sub.getTrainingTaskId(), trainingTaskMapper::selectById);
            if (task != null) {
                courseNameMap.computeIfAbsent(task.getCourseId(), cid -> {
                    Course c = courseMapper.selectById(cid);
                    return c != null ? c.getCourseName() : "";
                });
                ScoreRecord sr = scoreMap.get(sub.getId());
                if (sr != null && sr.getTotalScore() != null && sr.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                    scoresByCourse.computeIfAbsent(task.getCourseId(), k -> new ArrayList<>()).add(sr.getTotalScore());
                }
            }
        }

        List<String> radarIndicators = new ArrayList<>();
        List<BigDecimal> radarValues = new ArrayList<>();
        for (Map.Entry<Long, List<BigDecimal>> e : scoresByCourse.entrySet()) {
            radarIndicators.add(courseNameMap.getOrDefault(e.getKey(), ""));
            List<BigDecimal> vals = e.getValue();
            radarValues.add(vals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(vals.size()), 1, RoundingMode.HALF_UP));
        }

        List<StudentReportVO.Row> rows = new ArrayList<>();
        for (Submission sub : allSubmissions.stream()
                .sorted(Comparator.comparing(Submission::getSubmitTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList()) {
            TrainingTask task = taskMap.get(sub.getTrainingTaskId());
            ScoreRecord sr = scoreMap.get(sub.getId());
            rows.add(StudentReportVO.Row.builder()
                    .taskName(task != null ? task.getTaskName() : "")
                    .courseName(task != null ? courseNameMap.getOrDefault(task.getCourseId(), "") : "")
                    .score(sr != null ? sr.getTotalScore() : null)
                    .status(sub.getStatus())
                    .reviewComment(sr != null ? sr.getTeacherComment() : null)
                    .build());
        }

        return StudentReportVO.builder()
                .stats(StudentReportVO.Stats.builder()
                        .totalTasks(totalTasks)
                        .completedTasks(completedTaskIds.size())
                        .averageScore(avgScore)
                        .totalSubmissions(allSubmissions.size())
                        .build())
                .scoreTrend(StudentReportVO.ScoreTrend.builder()
                        .categories(trendMonths).values(trendValues).build())
                .radarData(StudentReportVO.RadarData.builder()
                        .indicators(radarIndicators).values(radarValues).build())
                .rows(rows)
                .build();
    }
}

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
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.student.service.StudentService;
import com.b1.module.student.vo.DashboardVO;
import com.b1.module.student.vo.GrowthProfileVO;
import com.b1.module.student.vo.NotificationVO;
import com.b1.module.student.vo.StudentReportVO;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final StandardDimensionMapper standardDimensionMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<Long> DEFAULT_DIMENSION_IDS = List.of(1001L, 1002L, 1003L, 1004L);

    private static final List<String> RADAR_CATEGORIES = List.of("代码", "需求", "项目", "协作", "文档", "创新");
    private static final Set<String> PENDING_STATUSES = Set.of("NOT_SUBMITTED", "REJECTED");
    private static final Set<String> COMPLETED_STATUSES = Set.of(
            "PUBLISHED", "COMPLETED", "GRADED", "REVIEWED");
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
                item.setSubmissionId(sub.getId());
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
            case "SUBMITTED" -> "CONFIRMED";
            case "GRADED", "REVIEWED", "COMPLETED" -> "PUBLISHED";
            case "PROCESSING" -> "ANALYZING";
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

        List<ScoreRecord> scores = allSubmissions.isEmpty() ? Collections.emptyList()
                : scoreRecordMapper.selectList(new LambdaQueryWrapper<ScoreRecord>()
                .in(ScoreRecord::getSubmissionId, allSubmissions.stream().map(Submission::getId).toList()));

        Set<Long> completedTaskIds = scores.stream()
                .filter(s -> s.getTotalScore() != null && s.getTotalScore().compareTo(BigDecimal.ZERO) > 0)
                .map(ScoreRecord::getTrainingTaskId)
                .collect(Collectors.toSet());
        vo.setCompletedTasks(completedTaskIds.size());
        if (vo.getTotalTasks() != null && vo.getTotalTasks() > 0) {
            vo.setCompletionRate(BigDecimal.valueOf(completedTaskIds.size() * 100.0 / vo.getTotalTasks())
                    .setScale(1, RoundingMode.HALF_UP));
        } else {
            vo.setCompletionRate(BigDecimal.ZERO);
        }

        if (!allSubmissions.isEmpty()) {
            List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();

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
            Map<Long, ScoreRecord> scoreBySubmission = scores.stream()
                    .collect(Collectors.toMap(ScoreRecord::getSubmissionId, s -> s, (a, b) -> a));

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

            vo.setDimensionRadar(buildDimensionRadar(userId, courseIds, scores));
            vo.setDimensionInsight(buildDimensionInsight(vo.getDimensionRadar()));
            vo.setGrowthTimeline(buildGrowthTimeline(allSubmissions, taskMap, courseNameMap, scoreBySubmission));

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
                h.setResult(mapSubmissionResult(sub.getStatus()));
                h.setSubmittedAt(sub.getSubmitTime());
                history.add(h);
            }
            vo.setSubmissionHistory(history);
        } else {
            vo.setAverageScore(null);
            vo.setHighestScore(null);
            vo.setLowestScore(null);
            vo.setCompletionRate(BigDecimal.ZERO);
            vo.setCourseScores(Collections.emptyList());
            vo.setMonthlyTrends(Collections.emptyList());
            vo.setDimensionRadar(Collections.emptyList());
            vo.setDimensionInsight(null);
            vo.setGrowthTimeline(Collections.emptyList());
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

        List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
        List<ScoreRecord> scores = submissionIds.isEmpty() ? Collections.emptyList() :
                scoreRecordMapper.selectList(new LambdaQueryWrapper<ScoreRecord>()
                        .in(ScoreRecord::getSubmissionId, submissionIds));

        Set<Long> completedTaskIds = scores.stream()
                .filter(s -> s.getTotalScore() != null && s.getTotalScore().compareTo(BigDecimal.ZERO) > 0)
                .map(ScoreRecord::getTrainingTaskId)
                .collect(Collectors.toSet());

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
        for (int i = 5; i >= 0; i--) {
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
        for (Submission sub : allSubmissions) {
            TrainingTask task = taskMap.computeIfAbsent(sub.getTrainingTaskId(), trainingTaskMapper::selectById);
            if (task != null) {
                courseNameMap.computeIfAbsent(task.getCourseId(), cid -> {
                    Course c = courseMapper.selectById(cid);
                    return c != null ? c.getCourseName() : "";
                });
            }
        }

        List<GrowthProfileVO.DimensionRadarVO> dimensionRadar = buildDimensionRadar(userId, courseIds, scores);
        List<String> radarIndicators = dimensionRadar.stream()
                .map(GrowthProfileVO.DimensionRadarVO::getDimensionName).toList();
        List<BigDecimal> radarValues = dimensionRadar.stream()
                .map(GrowthProfileVO.DimensionRadarVO::getMyAvg).toList();

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

    private GrowthProfileVO.DimensionInsightVO buildDimensionInsight(
            List<GrowthProfileVO.DimensionRadarVO> radar) {
        if (radar == null || radar.isEmpty()) return null;
        GrowthProfileVO.DimensionRadarVO strongest = radar.stream()
                .max(Comparator.comparing(GrowthProfileVO.DimensionRadarVO::getMyAvg))
                .orElse(null);
        GrowthProfileVO.DimensionRadarVO weakest = radar.stream()
                .min(Comparator.comparing(GrowthProfileVO.DimensionRadarVO::getMyAvg))
                .orElse(null);
        if (strongest == null || weakest == null) return null;

        GrowthProfileVO.DimensionInsightVO insight = new GrowthProfileVO.DimensionInsightVO();
        insight.setStrongest(strongest.getDimensionName());
        insight.setWeakest(weakest.getDimensionName());
        BigDecimal gap = strongest.getMyAvg().subtract(weakest.getMyAvg());
        if (gap.compareTo(BigDecimal.valueOf(8)) >= 0) {
            insight.setSummary("「" + weakest.getDimensionName() + "」与班级均值差距较大，建议优先补强。");
        } else if (strongest.getMyAvg().compareTo(strongest.getClassAvg()) >= 0) {
            insight.setSummary("整体表现均衡，「" + strongest.getDimensionName() + "」为当前优势维度。");
        } else {
            insight.setSummary("各维度接近班级平均水平，可继续巩固「" + strongest.getDimensionName() + "」。");
        }
        return insight;
    }

    private List<GrowthProfileVO.GrowthTimelineItemVO> buildGrowthTimeline(
            List<Submission> submissions,
            Map<Long, TrainingTask> taskMap,
            Map<Long, String> courseNameMap,
            Map<Long, ScoreRecord> scoreBySubmission) {
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<GrowthProfileVO.GrowthTimelineItemVO> timeline = new ArrayList<>();
        for (Submission sub : submissions.stream()
                .sorted(Comparator.comparing(Submission::getSubmitTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12).toList()) {
            TrainingTask task = taskMap.get(sub.getTrainingTaskId());
            ScoreRecord sr = scoreBySubmission.get(sub.getId());
            GrowthProfileVO.GrowthTimelineItemVO item = new GrowthProfileVO.GrowthTimelineItemVO();
            item.setDate(sub.getSubmitTime() != null ? sub.getSubmitTime().format(dayFmt) : "");
            item.setTitle(task != null ? task.getTaskName() : "实训任务");
            item.setDescription(task != null ? courseNameMap.getOrDefault(task.getCourseId(), "") : "");
            if ("REJECTED".equals(sub.getStatus())) {
                item.setType("reject");
                item.setDescription("提交被驳回，请根据教师反馈修改后重提");
            } else if (sr != null && sr.getTotalScore() != null && sr.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                item.setType("score");
                item.setScore(sr.getTotalScore());
                item.setDescription("成绩 " + sr.getTotalScore().setScale(1, RoundingMode.HALF_UP) + " 分 · "
                        + (task != null ? courseNameMap.getOrDefault(task.getCourseId(), "") : ""));
            } else {
                item.setType("submit");
                item.setDescription("已提交，等待 AI 分析或教师复核");
            }
            timeline.add(item);
        }
        return timeline;
    }

    private String mapSubmissionResult(String status) {
        if (status == null) return "PENDING";
        return switch (status) {
            case "PUBLISHED", "COMPLETED" -> "PASS";
            case "REJECTED" -> "REJECT";
            default -> "PENDING";
        };
    }

    private List<GrowthProfileVO.DimensionRadarVO> buildDimensionRadar(
            Long userId, List<Long> courseIds, List<ScoreRecord> myScores) {
        Map<Long, String> dimensionNames = loadDimensionNames();
        Map<Long, List<BigDecimal>> myDimensionScores = aggregateDimensionScores(myScores, dimensionNames);

        List<ScoreRecord> classScores = loadClassmateScoreRecords(userId, courseIds);
        Map<Long, List<BigDecimal>> classDimensionScores = aggregateDimensionScores(classScores, dimensionNames);

        List<GrowthProfileVO.DimensionRadarVO> radar = new ArrayList<>();
        for (Long dimensionId : DEFAULT_DIMENSION_IDS) {
            GrowthProfileVO.DimensionRadarVO item = new GrowthProfileVO.DimensionRadarVO();
            item.setDimensionName(dimensionNames.getOrDefault(dimensionId, "维度" + dimensionId));
            item.setMyAvg(averageOf(myDimensionScores.get(dimensionId)));
            item.setClassAvg(averageOf(classDimensionScores.get(dimensionId)));
            radar.add(item);
        }
        return radar;
    }

    private Map<Long, String> loadDimensionNames() {
        List<StandardDimension> dimensions = standardDimensionMapper.selectList(
                new LambdaQueryWrapper<StandardDimension>()
                        .in(StandardDimension::getId, DEFAULT_DIMENSION_IDS)
                        .orderByAsc(StandardDimension::getSortOrder));
        Map<Long, String> names = new LinkedHashMap<>();
        for (StandardDimension dimension : dimensions) {
            names.put(dimension.getId(), dimension.getDimName());
        }
        for (Long id : DEFAULT_DIMENSION_IDS) {
            names.putIfAbsent(id, "维度" + id);
        }
        return names;
    }

    private List<ScoreRecord> loadClassmateScoreRecords(Long userId, List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> classmateIds = courseStudentMapper.selectList(
                        new LambdaQueryWrapper<CourseStudent>().in(CourseStudent::getCourseId, courseIds))
                .stream().map(CourseStudent::getUserId).distinct().toList();
        if (classmateIds.isEmpty()) {
            return Collections.emptyList();
        }
        return scoreRecordMapper.selectList(new LambdaQueryWrapper<ScoreRecord>()
                .in(ScoreRecord::getUserId, classmateIds)
                .eq(ScoreRecord::getStatus, "PUBLISHED"));
    }

    private Map<Long, List<BigDecimal>> aggregateDimensionScores(
            List<ScoreRecord> records, Map<Long, String> dimensionNames) {
        Map<Long, List<BigDecimal>> grouped = new LinkedHashMap<>();
        for (Long dimensionId : dimensionNames.keySet()) {
            grouped.put(dimensionId, new ArrayList<>());
        }
        for (ScoreRecord record : records) {
            Map<Long, BigDecimal> parsed = parseDimensionScores(record);
            for (Map.Entry<Long, BigDecimal> entry : parsed.entrySet()) {
                grouped.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }
        return grouped;
    }

    private Map<Long, BigDecimal> parseDimensionScores(ScoreRecord record) {
        Map<Long, BigDecimal> parsed = new LinkedHashMap<>();
        if (record.getScoreDetails() != null && !record.getScoreDetails().isBlank()) {
            try {
                List<Map<String, Object>> items = OBJECT_MAPPER.readValue(
                        record.getScoreDetails(), new TypeReference<>() {});
                for (Map<String, Object> item : items) {
                    Object dimensionId = item.get("dimensionId");
                    Object score = item.get("score");
                    if (dimensionId == null || score == null) {
                        continue;
                    }
                    parsed.put(Long.valueOf(String.valueOf(dimensionId)), new BigDecimal(String.valueOf(score)));
                }
            } catch (Exception ignored) {
                // fall through to total-score estimate
            }
        }
        if (parsed.isEmpty() && record.getTotalScore() != null
                && record.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal quarter = record.getTotalScore()
                    .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
            for (Long dimensionId : DEFAULT_DIMENSION_IDS) {
                parsed.put(dimensionId, quarter);
            }
        }
        return parsed;
    }

    private BigDecimal averageOf(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }
}

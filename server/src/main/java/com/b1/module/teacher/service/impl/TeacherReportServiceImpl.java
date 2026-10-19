package com.b1.module.teacher.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.module.admin.entity.ClassInfo;
import com.b1.module.admin.mapper.ClassInfoMapper;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseClass;
import com.b1.module.course.entity.CourseStudent;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseClassMapper;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseStudentMapper;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.service.TeacherReportService;
import com.b1.module.teacher.vo.ClassReportVO;
import com.b1.module.teacher.vo.CollegeReportVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherReportServiceImpl implements TeacherReportService {

    private static final List<Long> DIMENSION_IDS = List.of(1001L, 1002L, 1003L, 1004L);
    private static final String[] BUCKET_LABELS = {"0-59", "60-69", "70-79", "80-89", "90-100"};

    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final CourseClassMapper courseClassMapper;
    private final ClassInfoMapper classInfoMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final SubmissionMapper submissionMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final StandardDimensionMapper standardDimensionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ClassReportVO getClassReport(String adminClass, String courseName) {
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Long> courseIds = getTeacherCourseIds(teacherId);
        if (courseIds.isEmpty()) return buildEmptyReport();

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Map<Long, String> courseClassNames = buildCourseClassNameMap(courseIds);

        Set<Long> filteredCourseIds = courseIds.stream()
                .filter(cid -> {
                    Course course = courseMap.get(cid);
                    if (course == null) return false;
                    String ac = courseClassNames.getOrDefault(cid, "");
                    return matchesFilters(adminClass, courseName, ac, course.getCourseName());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (filteredCourseIds.isEmpty()) return buildEmptyReport();

        List<CourseStudent> enrollments = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().in(CourseStudent::getCourseId, filteredCourseIds));
        if (enrollments.isEmpty()) return buildEmptyReport();

        Map<Long, User> userMap = new LinkedHashMap<>();
        Map<Long, CourseStudent> enrollmentByUser = new HashMap<>();
        for (CourseStudent cs : enrollments) {
            User user = userMapper.selectById(cs.getUserId());
            if (user == null) continue;
            userMap.putIfAbsent(user.getId(), user);
            enrollmentByUser.putIfAbsent(user.getId(), cs);
        }
        if (userMap.isEmpty()) return buildEmptyReport();

        List<TrainingTask> tasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>().in(TrainingTask::getCourseId, filteredCourseIds));
        Map<Long, TrainingTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(TrainingTask::getId, t -> t));
        List<Long> taskIds = tasks.stream().map(TrainingTask::getId).toList();

        List<Submission> submissions = taskIds.isEmpty() ? Collections.emptyList()
                : submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .in(Submission::getTrainingTaskId, taskIds)
                .in(Submission::getUserId, userMap.keySet()));

        Map<Long, List<Submission>> userSubmissions = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getUserId));

        List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
        List<ScoreRecord> scores = submissionIds.isEmpty() ? Collections.emptyList()
                : scoreRecordMapper.selectList(new LambdaQueryWrapper<ScoreRecord>()
                .in(ScoreRecord::getSubmissionId, submissionIds));
        Map<Long, ScoreRecord> scoreBySubmission = scores.stream()
                .collect(Collectors.toMap(ScoreRecord::getSubmissionId, s -> s, (a, b) -> a));

        List<ClassReportVO.Row> rows = new ArrayList<>();
        List<BigDecimal> allAvgScores = new ArrayList<>();
        int totalReviewed = 0;

        for (Map.Entry<Long, User> entry : userMap.entrySet()) {
            Long uid = entry.getKey();
            User user = entry.getValue();
            CourseStudent cs = enrollmentByUser.get(uid);
            Course course = cs != null ? courseMap.get(cs.getCourseId()) : null;
            String rowAdminClass = cs != null ? courseClassNames.getOrDefault(cs.getCourseId(), "") : "";
            String courseLabel = course != null ? course.getCourseName() : "";

            List<Submission> userSubs = userSubmissions.getOrDefault(uid, Collections.emptyList());
            List<BigDecimal> userScores = userSubs.stream()
                    .map(s -> scoreBySubmission.get(s.getId()))
                    .filter(Objects::nonNull)
                    .map(ScoreRecord::getTotalScore)
                    .filter(Objects::nonNull)
                    .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                    .toList();

            BigDecimal avg = average(userScores);
            BigDecimal max = userScores.isEmpty() ? BigDecimal.ZERO : Collections.max(userScores);
            BigDecimal min = userScores.isEmpty() ? BigDecimal.ZERO : Collections.min(userScores);

            if (!userScores.isEmpty()) {
                allAvgScores.add(avg);
                totalReviewed++;
            }

            rows.add(ClassReportVO.Row.builder()
                    .studentId(uid.toString())
                    .name(user.getRealName())
                    .className(rowAdminClass.isEmpty() ? courseLabel : rowAdminClass)
                    .courseName(courseLabel)
                    .completedCount(userSubs.size())
                    .avgScore(avg)
                    .maxScore(max)
                    .minScore(min)
                    .build());
        }

        int totalStudents = userMap.size();
        BigDecimal classAverage = average(allAvgScores);
        long passCount = allAvgScores.stream().filter(s -> s.compareTo(BigDecimal.valueOf(60)) >= 0).count();
        BigDecimal passRate = allAvgScores.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(passCount * 100.0 / allAvgScores.size()).setScale(1, RoundingMode.HALF_UP);

        int[] buckets = new int[5];
        for (BigDecimal s : allAvgScores) {
            double v = s.doubleValue();
            if (v < 60) buckets[0]++;
            else if (v < 70) buckets[1]++;
            else if (v < 80) buckets[2]++;
            else if (v < 90) buckets[3]++;
            else buckets[4]++;
        }

        Map<Long, List<BigDecimal>> courseScoreMap = new LinkedHashMap<>();
        for (Submission sub : submissions) {
            ScoreRecord sr = scoreBySubmission.get(sub.getId());
            if (sr == null || sr.getTotalScore() == null || sr.getTotalScore().compareTo(BigDecimal.ZERO) <= 0) continue;
            TrainingTask task = taskMap.get(sub.getTrainingTaskId());
            if (task == null) continue;
            courseScoreMap.computeIfAbsent(task.getCourseId(), k -> new ArrayList<>()).add(sr.getTotalScore());
        }

        List<String> courseLabels = new ArrayList<>();
        List<BigDecimal> courseAvgValues = new ArrayList<>();
        for (Map.Entry<Long, List<BigDecimal>> e : courseScoreMap.entrySet()) {
            Course c = courseMap.get(e.getKey());
            if (c == null) continue;
            courseLabels.add(c.getCourseName());
            courseAvgValues.add(average(e.getValue()));
        }

        Map<Long, Integer> studentsPerCourse = enrollments.stream()
                .filter(cs -> userMap.containsKey(cs.getUserId()))
                .collect(Collectors.groupingBy(CourseStudent::getCourseId,
                        Collectors.collectingAndThen(Collectors.mapping(CourseStudent::getUserId, Collectors.toSet()), Set::size)));

        List<String> taskLabels = new ArrayList<>();
        List<Integer> taskCompleted = new ArrayList<>();
        List<Integer> taskTotals = new ArrayList<>();
        int totalCompletedSlots = 0;
        int totalTaskSlots = 0;
        for (TrainingTask task : tasks) {
            int total = studentsPerCourse.getOrDefault(task.getCourseId(), 0);
            Set<Long> completedUsers = submissions.stream()
                    .filter(s -> s.getTrainingTaskId().equals(task.getId()))
                    .map(Submission::getUserId)
                    .collect(Collectors.toSet());
            int completed = (int) completedUsers.stream().filter(userMap::containsKey).count();
            taskLabels.add(task.getTaskName());
            taskCompleted.add(completed);
            taskTotals.add(total);
            totalCompletedSlots += completed;
            totalTaskSlots += total;
        }

        BigDecimal taskCompletionRate = totalTaskSlots == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalCompletedSlots * 100.0 / totalTaskSlots).setScale(1, RoundingMode.HALF_UP);

        Map<Long, String> dimensionNames = loadDimensionNames();
        Map<Long, List<BigDecimal>> dimensionScores = aggregateDimensionScores(scores, dimensionNames);
        List<String> dimLabels = new ArrayList<>();
        List<BigDecimal> dimValues = new ArrayList<>();
        for (Long dimId : DIMENSION_IDS) {
            dimLabels.add(dimensionNames.getOrDefault(dimId, "维度" + dimId));
            dimValues.add(average(dimensionScores.get(dimId)));
        }

        return ClassReportVO.builder()
                .stats(ClassReportVO.Stats.builder()
                        .totalStudents(totalStudents)
                        .totalReviewed(totalReviewed)
                        .totalTasks(tasks.size())
                        .classAverage(classAverage)
                        .passRate(passRate)
                        .taskCompletionRate(taskCompletionRate)
                        .build())
                .histogram(ClassReportVO.Histogram.builder()
                        .categories(Arrays.asList(BUCKET_LABELS))
                        .values(Arrays.stream(buckets).boxed().toList())
                        .build())
                .courseAvgs(ClassReportVO.CourseAvgs.builder()
                        .categories(courseLabels)
                        .values(courseAvgValues)
                        .build())
                .taskCompletion(ClassReportVO.TaskCompletion.builder()
                        .categories(taskLabels)
                        .completedCount(taskCompleted)
                        .totalStudents(taskTotals)
                        .build())
                .dimensionAvgs(ClassReportVO.DimensionAvgs.builder()
                        .categories(dimLabels)
                        .values(dimValues)
                        .build())
                .rows(rows)
                .build();
    }

    @Override
    public CollegeReportVO getCollegeReport() {
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Long> courseIds = getTeacherCourseIds(teacherId);
        if (courseIds.isEmpty()) return buildEmptyCollegeReport();

        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        Map<Long, String> courseClassNames = buildCourseClassNameMap(courseIds);

        List<String> classNames = new ArrayList<>();
        List<BigDecimal> classAvgs = new ArrayList<>();
        List<CollegeReportVO.ClassDetail> classDetails = new ArrayList<>();

        for (Course course : courses) {
            CourseMetrics metrics = computeCourseMetrics(course);
            String adminClass = courseClassNames.getOrDefault(course.getId(), course.getCourseName());
            String label = buildCrossClassLabel(adminClass, course.getCourseName());

            classNames.add(label);
            classAvgs.add(metrics.avgScore);
            classDetails.add(CollegeReportVO.ClassDetail.builder()
                    .className(adminClass)
                    .courseName(course.getCourseName())
                    .semester(course.getSemester())
                    .studentCount(metrics.studentCount)
                    .avgScore(metrics.avgScore)
                    .passRate(metrics.passRate)
                    .maxScore(metrics.maxScore)
                    .build());
        }

        List<String> semesters = courses.stream()
                .map(Course::getSemester)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();

        Map<String, Map<String, List<BigDecimal>>> classSemesterScores = new LinkedHashMap<>();
        for (Course course : courses) {
            CourseMetrics metrics = computeCourseMetrics(course);
            if (metrics.avgScore.compareTo(BigDecimal.ZERO) <= 0) continue;
            String adminClassLabel = courseClassNames.getOrDefault(course.getId(), course.getCourseName());
            classSemesterScores
                    .computeIfAbsent(adminClassLabel, k -> new LinkedHashMap<>())
                    .computeIfAbsent(course.getSemester(), k -> new ArrayList<>())
                    .add(metrics.avgScore);
        }

        List<CollegeReportVO.SemesterTrend.Series> seriesList = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<BigDecimal>>> entry : classSemesterScores.entrySet()) {
            List<BigDecimal> data = semesters.stream()
                    .map(sem -> {
                        List<BigDecimal> vals = entry.getValue().get(sem);
                        return vals == null || vals.isEmpty() ? null : average(vals);
                    })
                    .toList();
            seriesList.add(CollegeReportVO.SemesterTrend.Series.builder()
                    .name(entry.getKey())
                    .data(data)
                    .build());
        }

        return CollegeReportVO.builder()
                .crossClass(CollegeReportVO.CrossClass.builder()
                        .classNames(classNames)
                        .values(classAvgs)
                        .build())
                .semesterTrend(CollegeReportVO.SemesterTrend.builder()
                        .semesters(semesters)
                        .series(seriesList)
                        .build())
                .classDetails(classDetails)
                .build();
    }

    private CourseMetrics computeCourseMetrics(Course course) {
        List<CourseStudent> students = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, course.getId()));
        List<Long> studentIds = students.stream().map(CourseStudent::getUserId).toList();
        if (studentIds.isEmpty()) {
            return CourseMetrics.empty();
        }

        List<TrainingTask> tasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>().eq(TrainingTask::getCourseId, course.getId()));
        List<Long> taskIds = tasks.stream().map(TrainingTask::getId).toList();
        if (taskIds.isEmpty()) {
            return new CourseMetrics(studentIds.size(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .in(Submission::getTrainingTaskId, taskIds)
                        .in(Submission::getUserId, studentIds));
        List<Long> subIds = submissions.stream().map(Submission::getId).toList();
        if (subIds.isEmpty()) {
            return new CourseMetrics(studentIds.size(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<ScoreRecord> scoreRecords = scoreRecordMapper.selectList(
                new LambdaQueryWrapper<ScoreRecord>().in(ScoreRecord::getSubmissionId, subIds));
        List<BigDecimal> totals = scoreRecords.stream()
                .map(ScoreRecord::getTotalScore)
                .filter(Objects::nonNull)
                .filter(s -> s.compareTo(BigDecimal.ZERO) > 0)
                .toList();

        BigDecimal avg = average(totals);
        BigDecimal max = totals.isEmpty() ? BigDecimal.ZERO : Collections.max(totals);
        long passCount = totals.stream().filter(s -> s.compareTo(BigDecimal.valueOf(60)) >= 0).count();
        BigDecimal passRate = totals.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(passCount * 100.0 / totals.size()).setScale(1, RoundingMode.HALF_UP);
        return new CourseMetrics(studentIds.size(), avg, passRate, max);
    }

    private Map<Long, String> buildCourseClassNameMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) return Collections.emptyMap();
        List<CourseClass> links = courseClassMapper.selectList(
                new LambdaQueryWrapper<CourseClass>().in(CourseClass::getCourseId, courseIds));
        Set<Long> classIds = links.stream().map(CourseClass::getClassId).collect(Collectors.toSet());
        Map<Long, ClassInfo> classMap = classIds.isEmpty() ? Collections.emptyMap()
                : classInfoMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(ClassInfo::getId, c -> c));
        Map<Long, String> result = new HashMap<>();
        for (CourseClass link : links) {
            ClassInfo info = classMap.get(link.getClassId());
            if (info != null && StringUtils.hasText(info.getClassName())) {
                result.put(link.getCourseId(), info.getClassName());
            }
        }
        return result;
    }

    private boolean matchesFilters(String adminClassFilter, String courseFilter,
                                   String adminClass, String courseName) {
        if (StringUtils.hasText(adminClassFilter) && !adminClassFilter.equals(adminClass)) {
            return false;
        }
        if (StringUtils.hasText(courseFilter) && !courseFilter.equals(courseName)) {
            return false;
        }
        return true;
    }

    private String buildCrossClassLabel(String adminClass, String courseName) {
        if (!StringUtils.hasText(adminClass) || adminClass.equals(courseName)) {
            return courseName;
        }
        return adminClass + " · " + courseName;
    }

    private Map<Long, String> loadDimensionNames() {
        List<StandardDimension> dimensions = standardDimensionMapper.selectList(
                new LambdaQueryWrapper<StandardDimension>()
                        .in(StandardDimension::getId, DIMENSION_IDS)
                        .orderByAsc(StandardDimension::getSortOrder));
        Map<Long, String> names = new LinkedHashMap<>();
        for (StandardDimension dimension : dimensions) {
            names.put(dimension.getId(), dimension.getDimName());
        }
        for (Long id : DIMENSION_IDS) {
            names.putIfAbsent(id, "维度" + id);
        }
        return names;
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
                List<Map<String, Object>> items = objectMapper.readValue(
                        record.getScoreDetails(), new TypeReference<>() {});
                for (Map<String, Object> item : items) {
                    Object dimensionId = item.get("dimensionId");
                    Object score = item.get("score");
                    if (dimensionId == null || score == null) continue;
                    parsed.put(Long.valueOf(String.valueOf(dimensionId)), new BigDecimal(String.valueOf(score)));
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        if (parsed.isEmpty() && record.getTotalScore() != null
                && record.getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal quarter = record.getTotalScore()
                    .divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
            for (Long dimensionId : DIMENSION_IDS) {
                parsed.put(dimensionId, quarter);
            }
        }
        return parsed;
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return BigDecimal.ZERO;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }

    private List<Long> getTeacherCourseIds(Long teacherId) {
        return courseTeacherMapper.selectList(
                        new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getUserId, teacherId))
                .stream().map(CourseTeacher::getCourseId).toList();
    }

    private ClassReportVO buildEmptyReport() {
        return ClassReportVO.builder()
                .stats(ClassReportVO.Stats.builder()
                        .totalStudents(0).totalReviewed(0).totalTasks(0)
                        .classAverage(BigDecimal.ZERO).passRate(BigDecimal.ZERO)
                        .taskCompletionRate(BigDecimal.ZERO).build())
                .histogram(ClassReportVO.Histogram.builder()
                        .categories(Arrays.asList(BUCKET_LABELS))
                        .values(Arrays.asList(0, 0, 0, 0, 0)).build())
                .courseAvgs(ClassReportVO.CourseAvgs.builder()
                        .categories(Collections.emptyList()).values(Collections.emptyList()).build())
                .taskCompletion(ClassReportVO.TaskCompletion.builder()
                        .categories(Collections.emptyList())
                        .completedCount(Collections.emptyList())
                        .totalStudents(Collections.emptyList()).build())
                .dimensionAvgs(ClassReportVO.DimensionAvgs.builder()
                        .categories(Collections.emptyList()).values(Collections.emptyList()).build())
                .rows(Collections.emptyList())
                .build();
    }

    private CollegeReportVO buildEmptyCollegeReport() {
        return CollegeReportVO.builder()
                .crossClass(CollegeReportVO.CrossClass.builder()
                        .classNames(Collections.emptyList()).values(Collections.emptyList()).build())
                .semesterTrend(CollegeReportVO.SemesterTrend.builder()
                        .semesters(Collections.emptyList()).series(Collections.emptyList()).build())
                .classDetails(Collections.emptyList())
                .build();
    }

    private record CourseMetrics(int studentCount, BigDecimal avgScore, BigDecimal passRate, BigDecimal maxScore) {
        static CourseMetrics empty() {
            return new CourseMetrics(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}

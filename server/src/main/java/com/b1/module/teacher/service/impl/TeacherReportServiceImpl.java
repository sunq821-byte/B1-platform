package com.b1.module.teacher.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseStudent;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseStudentMapper;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.service.TeacherReportService;
import com.b1.module.teacher.vo.ClassReportVO;
import com.b1.module.teacher.vo.CollegeReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherReportServiceImpl implements TeacherReportService {

    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final SubmissionMapper submissionMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final TrainingTaskMapper trainingTaskMapper;

    @Override
    public ClassReportVO getClassReport(String className) {
        Long teacherId = StpUtil.getLoginIdAsLong();
        List<Long> courseIds = getTeacherCourseIds(teacherId);
        if (courseIds.isEmpty()) return buildEmptyReport();

        // students in teacher's courses
        List<CourseStudent> enrollments = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().in(CourseStudent::getCourseId, courseIds));
        if (enrollments.isEmpty()) return buildEmptyReport();

        List<Long> studentUserIds = enrollments.stream()
                .map(CourseStudent::getUserId).distinct().collect(Collectors.toList());

        // courses map
        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // users map (filter by className if specified)
        List<User> users = userMapper.selectBatchIds(studentUserIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User u : users) {
            CourseStudent cs = enrollments.stream()
                    .filter(e -> e.getUserId().equals(u.getId())).findFirst().orElse(null);
            if (cs == null) continue;
            Course course = courseMap.get(cs.getCourseId());
            String userClassName = course != null ? course.getCourseName() : "";
            if (className != null && !className.isEmpty() && !className.equals(userClassName)) continue;
            userMap.put(u.getId(), u);
        }

        // tasks for teacher's courses
        List<TrainingTask> tasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>().in(TrainingTask::getCourseId, courseIds));
        Map<Long, TrainingTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(TrainingTask::getId, t -> t));
        List<Long> taskIds = tasks.stream().map(TrainingTask::getId).collect(Collectors.toList());

        // submissions
        List<Submission> submissions = taskIds.isEmpty() ? Collections.emptyList() :
                submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                        .in(Submission::getTrainingTaskId, taskIds)
                        .in(Submission::getUserId, userMap.keySet()));

        Map<Long, List<Submission>> userSubmissions = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getUserId));

        // score records
        List<Long> submissionIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());
        List<ScoreRecord> scores = submissionIds.isEmpty() ? Collections.emptyList() :
                scoreRecordMapper.selectList(new LambdaQueryWrapper<ScoreRecord>()
                        .in(ScoreRecord::getSubmissionId, submissionIds));
        Map<Long, ScoreRecord> scoreBySubmission = scores.stream()
                .collect(Collectors.toMap(ScoreRecord::getSubmissionId, s -> s, (a, b) -> a));

        // build rows
        List<ClassReportVO.Row> rows = new ArrayList<>();
        List<BigDecimal> allAvgScores = new ArrayList<>();
        int totalReviewed = 0;

        for (Map.Entry<Long, User> entry : userMap.entrySet()) {
            Long uid = entry.getKey();
            User user = entry.getValue();
            List<Submission> userSubs = userSubmissions.getOrDefault(uid, Collections.emptyList());
            int completed = userSubs.size();

            List<BigDecimal> userScores = userSubs.stream()
                    .map(s -> scoreBySubmission.get(s.getId()))
                    .filter(Objects::nonNull)
                    .map(ScoreRecord::getTotalScore)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            BigDecimal avg = userScores.isEmpty() ? BigDecimal.ZERO :
                    userScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(userScores.size()), 1, RoundingMode.HALF_UP);
            BigDecimal max = userScores.isEmpty() ? BigDecimal.ZERO : Collections.max(userScores);
            BigDecimal min = userScores.isEmpty() ? BigDecimal.ZERO : Collections.min(userScores);

            if (!userScores.isEmpty()) {
                allAvgScores.add(avg);
                totalReviewed++;
            }

            // get user's className from enrollment
            String userClassName = enrollments.stream()
                    .filter(e -> e.getUserId().equals(uid))
                    .findFirst()
                    .map(e -> {
                        Course c = courseMap.get(e.getCourseId());
                        return c != null ? c.getCourseName() : "";
                    }).orElse("");

            rows.add(ClassReportVO.Row.builder()
                    .studentId(uid.toString())
                    .name(user.getRealName())
                    .className(userClassName)
                    .completedCount(completed)
                    .avgScore(avg)
                    .maxScore(max)
                    .minScore(min)
                    .build());
        }

        // stats
        int totalStudents = userMap.size();
        BigDecimal classAverage = allAvgScores.isEmpty() ? BigDecimal.ZERO :
                allAvgScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(allAvgScores.size()), 1, RoundingMode.HALF_UP);
        long passCount = allAvgScores.stream().filter(s -> s.compareTo(BigDecimal.valueOf(60)) >= 0).count();
        BigDecimal passRate = allAvgScores.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(passCount * 100.0 / allAvgScores.size()).setScale(1, RoundingMode.HALF_UP);

        // histogram
        int[] buckets = new int[5];
        String[] bucketLabels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        for (BigDecimal s : allAvgScores) {
            double v = s.doubleValue();
            if (v < 60) buckets[0]++;
            else if (v < 70) buckets[1]++;
            else if (v < 80) buckets[2]++;
            else if (v < 90) buckets[3]++;
            else buckets[4]++;
        }

        // course avgs
        Map<Long, List<BigDecimal>> courseScores = new LinkedHashMap<>();
        for (Submission sub : submissions) {
            ScoreRecord sr = scoreBySubmission.get(sub.getId());
            if (sr == null || sr.getTotalScore() == null) continue;
            TrainingTask task = taskMap.get(sub.getTrainingTaskId());
            if (task == null) continue;
            courseScores.computeIfAbsent(task.getCourseId(), k -> new ArrayList<>()).add(sr.getTotalScore());
        }

        List<String> courseLabels = new ArrayList<>();
        List<BigDecimal> courseAvgValues = new ArrayList<>();
        for (Map.Entry<Long, List<BigDecimal>> e : courseScores.entrySet()) {
            Course c = courseMap.get(e.getKey());
            if (c == null) continue;
            BigDecimal avg = e.getValue().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(e.getValue().size()), 1, RoundingMode.HALF_UP);
            courseLabels.add(c.getCourseName());
            courseAvgValues.add(avg);
        }

        return ClassReportVO.builder()
                .stats(ClassReportVO.Stats.builder()
                        .totalStudents(totalStudents)
                        .totalReviewed(totalReviewed)
                        .classAverage(classAverage)
                        .passRate(passRate)
                        .build())
                .histogram(ClassReportVO.Histogram.builder()
                        .categories(Arrays.asList(bucketLabels))
                        .values(Arrays.stream(buckets).boxed().collect(Collectors.toList()))
                        .build())
                .courseAvgs(ClassReportVO.CourseAvgs.builder()
                        .categories(courseLabels)
                        .values(courseAvgValues)
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
        // group by course (as class proxy)
        List<String> classNames = new ArrayList<>();
        List<BigDecimal> classAvgs = new ArrayList<>();

        for (Course course : courses) {
            List<CourseStudent> students = courseStudentMapper.selectList(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, course.getId()));
            List<Long> studentIds = students.stream().map(CourseStudent::getUserId).collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                classNames.add(course.getCourseName());
                classAvgs.add(BigDecimal.ZERO);
                continue;
            }

            List<TrainingTask> tasks = trainingTaskMapper.selectList(
                    new LambdaQueryWrapper<TrainingTask>().eq(TrainingTask::getCourseId, course.getId()));
            List<Long> taskIds = tasks.stream().map(TrainingTask::getId).collect(Collectors.toList());
            if (taskIds.isEmpty()) {
                classNames.add(course.getCourseName());
                classAvgs.add(BigDecimal.ZERO);
                continue;
            }

            List<Submission> submissions = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .in(Submission::getTrainingTaskId, taskIds)
                            .in(Submission::getUserId, studentIds));
            List<Long> subIds = submissions.stream().map(Submission::getId).collect(Collectors.toList());
            if (subIds.isEmpty()) {
                classNames.add(course.getCourseName());
                classAvgs.add(BigDecimal.ZERO);
                continue;
            }

            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>().in(ScoreRecord::getSubmissionId, subIds));
            List<BigDecimal> totals = scores.stream()
                    .map(ScoreRecord::getTotalScore).filter(Objects::nonNull).collect(Collectors.toList());

            BigDecimal avg = totals.isEmpty() ? BigDecimal.ZERO :
                    totals.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(totals.size()), 1, RoundingMode.HALF_UP);

            classNames.add(course.getCourseName());
            classAvgs.add(avg);
        }

        // semester trend — collect semester data
        List<String> semesters = courses.stream()
                .map(Course::getSemester).filter(Objects::nonNull).distinct().sorted().collect(Collectors.toList());

        List<CollegeReportVO.SemesterTrend.Series> seriesList = new ArrayList<>();
        for (String semester : semesters) {
            List<Course> semesterCourses = courses.stream()
                    .filter(c -> semester.equals(c.getSemester())).collect(Collectors.toList());
            List<BigDecimal> semesterData = new ArrayList<>();
            for (Course sc : semesterCourses) {
                int idx = classNames.indexOf(sc.getCourseName());
                if (idx >= 0) semesterData.add(classAvgs.get(idx));
            }
            if (!semesterData.isEmpty()) {
                seriesList.add(CollegeReportVO.SemesterTrend.Series.builder()
                        .name(semester).data(semesterData).build());
            }
        }

        return CollegeReportVO.builder()
                .crossClass(CollegeReportVO.CrossClass.builder()
                        .classNames(classNames)
                        .values(classAvgs)
                        .build())
                .semesterTrend(CollegeReportVO.SemesterTrend.builder()
                        .semesters(classNames)
                        .series(seriesList)
                        .build())
                .build();
    }

    private List<Long> getTeacherCourseIds(Long teacherId) {
        return courseTeacherMapper.selectList(
                        new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getUserId, teacherId))
                .stream().map(CourseTeacher::getCourseId).collect(Collectors.toList());
    }

    private ClassReportVO buildEmptyReport() {
        return ClassReportVO.builder()
                .stats(ClassReportVO.Stats.builder()
                        .totalStudents(0).totalReviewed(0)
                        .classAverage(BigDecimal.ZERO).passRate(BigDecimal.ZERO).build())
                .histogram(ClassReportVO.Histogram.builder()
                        .categories(Collections.emptyList()).values(Collections.emptyList()).build())
                .courseAvgs(ClassReportVO.CourseAvgs.builder()
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
                .build();
    }
}

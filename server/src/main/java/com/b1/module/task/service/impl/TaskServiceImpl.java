package com.b1.module.task.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.common.result.PageResult;
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
import com.b1.module.standard.entity.StandardDimension;
import com.b1.module.standard.mapper.StandardDimensionMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.task.service.TaskService;
import com.b1.module.task.vo.TaskDetailVO;
import com.b1.module.task.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TrainingTaskMapper trainingTaskMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final CourseMapper courseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final UserMapper userMapper;
    private final SubmissionMapper submissionMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final StandardDimensionMapper standardDimensionMapper;

    @Override
    public PageResult<TaskVO> listTasks(int page, int pageSize, String status, String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();

        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getUserId, userId));

        if (courseStudents.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        List<Long> courseIds = courseStudents.stream()
                .map(CourseStudent::getCourseId)
                .toList();

        LambdaQueryWrapper<TrainingTask> taskQuery = new LambdaQueryWrapper<TrainingTask>()
                .in(TrainingTask::getCourseId, courseIds)
                .eq(TrainingTask::getStatus, "PUBLISHED")
                .eq(TrainingTask::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            taskQuery.like(TrainingTask::getTaskName, keyword);
        }

        IPage<TrainingTask> taskPage = trainingTaskMapper.selectPage(
                new Page<>(page, pageSize), taskQuery);

        List<TrainingTask> tasks = taskPage.getRecords();

        Map<Long, String> courseNameMap = buildCourseNameMap(courseIds);
        Map<Long, String> teacherNameMap = buildTeacherNameMap(courseIds);

        List<Long> taskIds = tasks.stream().map(TrainingTask::getId).toList();
        Map<Long, Submission> latestSubmissionByTask = loadLatestSubmissions(userId, taskIds);
        Map<Long, ScoreRecord> scoreBySubmission = loadPublishedScores(latestSubmissionByTask.values());

        List<TaskVO> vos = new ArrayList<>();
        for (TrainingTask task : tasks) {
            String mySubmissionStatus = getMySubmissionStatus(userId, task.getId());

            if (StringUtils.hasText(status) && !status.equals(mySubmissionStatus)) {
                continue;
            }

            TaskVO vo = new TaskVO();
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getTaskName());
            vo.setCourseName(courseNameMap.getOrDefault(task.getCourseId(), ""));
            vo.setTeacherName(teacherNameMap.getOrDefault(task.getCourseId(), "待分配"));
            vo.setDeadline(task.getEndTime());
            vo.setTotalScore(task.getMaxScore());
            vo.setSubmissionType(task.getSubmissionType());
            vo.setStatus(task.getStatus());
            vo.setMySubmissionStatus(mySubmissionStatus);
            vo.setCreatedAt(task.getPublishTime());

            Submission mySubmission = latestSubmissionByTask.get(task.getId());
            if (mySubmission != null) {
                vo.setMySubmissionId(mySubmission.getId());
                if ("PUBLISHED".equals(mySubmission.getStatus())) {
                    ScoreRecord score = scoreBySubmission.get(mySubmission.getId());
                    if (score != null) {
                        vo.setMyScore(score.getTotalScore());
                    }
                }
            }

            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, taskPage.getTotal());
    }

    @Override
    public TaskDetailVO getTaskDetail(Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();

        TrainingTask task = requireVisibleTask(taskId, userId);

        TaskDetailVO vo = new TaskDetailVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setDescription(task.getDescription());
        vo.setRequirements(task.getRequirement());
        vo.setDeadline(task.getEndTime());
        vo.setTotalScore(task.getMaxScore());
        vo.setSubmissionType(task.getSubmissionType());
        vo.setSubmitLimit(task.getMaxAiAnalyzeCount());
        vo.setMaxSubmitCount(task.getMaxAiAnalyzeCount());
        vo.setMaxAiAnalyzeCount(task.getMaxAiAnalyzeCount());
        vo.setCreatedAt(task.getPublishTime());
        vo.setUpdatedAt(task.getUpdateTime());

        Course course = courseMapper.selectById(task.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }

        Map<Long, String> teacherNameMap = buildTeacherNameMap(List.of(task.getCourseId()));
        vo.setTeacherName(teacherNameMap.getOrDefault(task.getCourseId(), "待分配"));

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, task.getCourseId()));
        if (!courseTeachers.isEmpty()) {
            User teacher = userMapper.selectById(courseTeachers.get(0).getUserId());
            if (teacher != null) {
                vo.setTeacherEmail(teacher.getEmail());
            }
        }

        if (task.getStandardId() != null) {
            List<StandardDimension> dimensions = standardDimensionMapper.selectList(
                    new LambdaQueryWrapper<StandardDimension>()
                            .eq(StandardDimension::getStandardId, task.getStandardId())
                            .orderByAsc(StandardDimension::getSortOrder));
            List<TaskDetailVO.EvaluationDimensionVO> dimVOs = new ArrayList<>();
            for (StandardDimension dim : dimensions) {
                TaskDetailVO.EvaluationDimensionVO dimVO = new TaskDetailVO.EvaluationDimensionVO();
                dimVO.setDimensionName(dim.getDimName());
                dimVO.setWeight(dim.getWeight());
                dimVO.setMaxScore(dim.getMaxScore());
                dimVOs.add(dimVO);
            }
            vo.setEvaluationDimensions(dimVOs);
        } else {
            vo.setEvaluationDimensions(Collections.emptyList());
        }

        vo.setAttachments(Collections.emptyList());

        String mySubmissionStatus = getMySubmissionStatus(userId, taskId);
        vo.setMySubmissionStatus(mySubmissionStatus);

        Submission mySubmission = submissionMapper.selectOne(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getDeleted, 0));
        if (mySubmission != null) {
            vo.setMySubmissionId(mySubmission.getId());
            vo.setMySubmitCount(mySubmission.getAiAnalyzeUsedCount() != null
                    ? mySubmission.getAiAnalyzeUsedCount() : 0);
            if ("PUBLISHED".equals(mySubmission.getStatus())) {
                ScoreRecord score = scoreRecordMapper.selectOne(
                        new LambdaQueryWrapper<ScoreRecord>()
                                .eq(ScoreRecord::getSubmissionId, mySubmission.getId())
                                .last("LIMIT 1"));
                if (score != null) {
                    vo.setMyScore(score.getTotalScore());
                }
            }
        } else {
            vo.setMySubmitCount(0);
        }

        return vo;
    }

    private String getMySubmissionStatus(Long userId, Long taskId) {
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getDeleted, 0)
                        .orderByDesc(Submission::getSubmitCount)
                        .last("LIMIT 1"));
        if (submissions.isEmpty()) {
            return "NOT_SUBMITTED";
        }
        return submissions.get(0).getStatus();
    }

    private TrainingTask requireVisibleTask(Long taskId, Long userId) {
        TrainingTask task = trainingTaskMapper.selectOne(
                new LambdaQueryWrapper<TrainingTask>()
                        .eq(TrainingTask::getId, taskId)
                        .eq(TrainingTask::getDeleted, 0)
                        .eq(TrainingTask::getStatus, "PUBLISHED"));
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在或已下架");
        }

        Long enrolled = courseStudentMapper.selectCount(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getUserId, userId)
                        .eq(CourseStudent::getCourseId, task.getCourseId()));
        if (enrolled == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在或已下架");
        }
        return task;
    }

    private Map<Long, Submission> loadLatestSubmissions(Long userId, List<Long> taskIds) {
        if (taskIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .in(Submission::getTrainingTaskId, taskIds)
                        .eq(Submission::getDeleted, 0)
                        .orderByDesc(Submission::getSubmitCount));
        Map<Long, Submission> latestByTask = new HashMap<>();
        for (Submission submission : submissions) {
            latestByTask.putIfAbsent(submission.getTrainingTaskId(), submission);
        }
        return latestByTask;
    }

    private Map<Long, ScoreRecord> loadPublishedScores(java.util.Collection<Submission> submissions) {
        Set<Long> publishedSubmissionIds = submissions.stream()
                .filter(s -> "PUBLISHED".equals(s.getStatus()))
                .map(Submission::getId)
                .collect(Collectors.toSet());
        if (publishedSubmissionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return scoreRecordMapper.selectList(
                        new LambdaQueryWrapper<ScoreRecord>()
                                .in(ScoreRecord::getSubmissionId, publishedSubmissionIds))
                .stream()
                .collect(Collectors.toMap(ScoreRecord::getSubmissionId, sr -> sr, (a, b) -> a));
    }

    private Map<Long, String> buildCourseNameMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        return courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getCourseName, (a, b) -> a));
    }

    private Map<Long, String> buildTeacherNameMap(List<Long> courseIds) {
        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().in(CourseTeacher::getCourseId, courseIds));
        if (courseTeachers.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> teacherIds = courseTeachers.stream()
                .map(CourseTeacher::getUserId)
                .distinct()
                .toList();
        Map<Long, String> teacherNameById = userMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));
        return courseTeachers.stream()
                .collect(Collectors.toMap(
                        CourseTeacher::getCourseId,
                        ct -> teacherNameById.getOrDefault(ct.getUserId(), "待分配"),
                        (a, b) -> a));
    }
}
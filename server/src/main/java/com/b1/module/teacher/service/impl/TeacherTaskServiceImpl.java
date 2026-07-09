package com.b1.module.teacher.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.b1.common.constant.SystemConstants;
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
import com.b1.module.review.entity.TeacherReview;
import com.b1.module.review.mapper.TeacherReviewMapper;
import com.b1.module.standard.entity.EvaluationStandard;
import com.b1.module.standard.mapper.EvaluationStandardMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingClass;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingClassMapper;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.dto.TaskCreateDTO;
import com.b1.module.teacher.dto.TaskUpdateDTO;
import com.b1.module.teacher.service.TeacherTaskService;
import com.b1.module.teacher.vo.TaskDetailVO;
import com.b1.module.teacher.vo.TaskListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherTaskServiceImpl implements TeacherTaskService {

    private final TrainingTaskMapper trainingTaskMapper;
    private final TrainingClassMapper trainingClassMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseMapper courseMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final SubmissionMapper submissionMapper;
    private final TeacherReviewMapper teacherReviewMapper;
    private final EvaluationStandardMapper evaluationStandardMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<TaskListVO> listTasks(Long courseId, int page, int pageSize, String keyword, String status) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该课程");
        }

        LambdaQueryWrapper<TrainingTask> query = new LambdaQueryWrapper<TrainingTask>()
                .eq(TrainingTask::getCourseId, courseId)
                .eq(TrainingTask::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            query.like(TrainingTask::getTaskName, keyword);
        }
        if (StringUtils.hasText(status)) {
            query.eq(TrainingTask::getStatus, status);
        }
        query.orderByDesc(TrainingTask::getCreateTime);

        IPage<TrainingTask> taskPage = trainingTaskMapper.selectPage(
                new Page<>(page, pageSize), query);

        Course course = courseMapper.selectById(courseId);
        String courseName = course != null ? course.getCourseName() : "";

        List<TaskListVO> vos = new ArrayList<>();
        for (TrainingTask task : taskPage.getRecords()) {
            TaskListVO vo = new TaskListVO();
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getTaskName());
            vo.setCourseName(courseName);
            vo.setSubmissionType(task.getSubmissionType());
            vo.setMaxSubmitCount(task.getMaxSubmitCount());
            vo.setTotalScore(task.getMaxScore());
            vo.setStatus(task.getStatus());
            vo.setDeadline(task.getEndTime());
            vo.setPublishTime(task.getPublishTime());

            List<Submission> submissions = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .eq(Submission::getTrainingTaskId, task.getId())
                            .eq(Submission::getDeleted, 0));

            long submittedUserCount = submissions.stream()
                    .map(Submission::getUserId).distinct().count();
            vo.setSubmissionCount((int) submittedUserCount);

            if (!submissions.isEmpty()) {
                List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
                long reviewedCount = teacherReviewMapper.selectCount(
                        new LambdaQueryWrapper<TeacherReview>()
                                .in(TeacherReview::getSubmissionId, submissionIds));
                vo.setReviewedCount((int) reviewedCount);
            } else {
                vo.setReviewedCount(0);
            }

            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, taskPage.getTotal());
    }

    @Override
    public TaskDetailVO getTaskDetail(Long taskId) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该任务");
        }

        TaskDetailVO vo = new TaskDetailVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setDescription(task.getDescription());
        vo.setRequirement(task.getRequirement());
        vo.setSubmissionType(task.getSubmissionType());
        vo.setMaxSubmitCount(task.getMaxSubmitCount());
        vo.setAllowLate(task.getAllowLate());
        vo.setEndTime(task.getEndTime());
        vo.setPublishTime(task.getPublishTime());
        vo.setTotalScore(task.getMaxScore());
        vo.setStatus(task.getStatus());
        vo.setStandardId(task.getStandardId());

        Course course = courseMapper.selectById(task.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
        }

        if (task.getStandardId() != null) {
            EvaluationStandard standard = evaluationStandardMapper.selectById(task.getStandardId());
            if (standard != null) {
                vo.setStandardName(standard.getStandardName());
            }
        }

        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, task.getCourseId()));

        List<Long> studentUserIds = courseStudents.stream()
                .map(CourseStudent::getUserId).toList();
        Map<Long, User> userMap = Collections.emptyMap();
        if (!studentUserIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(studentUserIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        }

        List<Submission> allSubmissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getDeleted, 0));

        Map<Long, List<Submission>> submissionsByUser = allSubmissions.stream()
                .collect(Collectors.groupingBy(Submission::getUserId));

        Set<Long> reviewedSubmissionIds;
        if (!allSubmissions.isEmpty()) {
            List<Long> submissionIds = allSubmissions.stream().map(Submission::getId).toList();
            List<TeacherReview> reviews = teacherReviewMapper.selectList(
                    new LambdaQueryWrapper<TeacherReview>()
                            .in(TeacherReview::getSubmissionId, submissionIds));
            reviewedSubmissionIds = reviews.stream()
                    .map(TeacherReview::getSubmissionId).collect(Collectors.toSet());
        } else {
            reviewedSubmissionIds = Collections.emptySet();
        }

        List<TaskDetailVO.TaskStudentVO> studentVOs = new ArrayList<>();
        for (CourseStudent cs : courseStudents) {
            User u = userMap.get(cs.getUserId());
            if (u == null) continue;

            TaskDetailVO.TaskStudentVO sv = new TaskDetailVO.TaskStudentVO();
            sv.setUserId(u.getId());
            sv.setRealName(u.getRealName());
            sv.setEmail(u.getEmail());

            List<Submission> userSubs = submissionsByUser.getOrDefault(cs.getUserId(), Collections.emptyList());
            sv.setSubmitCount(userSubs.size());

            if (!userSubs.isEmpty()) {
                Submission latest = userSubs.stream()
                        .max(Comparator.comparing(Submission::getSubmitTime, Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null);
                if (latest != null) {
                    sv.setLatestStatus(latest.getStatus());
                    sv.setLatestSubmitTime(latest.getSubmitTime());
                }
                sv.setHasReview(userSubs.stream().anyMatch(s -> reviewedSubmissionIds.contains(s.getId())));
            } else {
                sv.setLatestStatus("NOT_SUBMITTED");
                sv.setHasReview(false);
            }

            studentVOs.add(sv);
        }
        vo.setStudents(studentVOs);

        return vo;
    }

    @Override
    public TaskListVO createTask(Long courseId, TaskCreateDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该课程");
        }

        TrainingTask task = new TrainingTask();
        task.setCourseId(courseId);
        task.setTaskName(dto.getTaskName());
        task.setSubmissionType(dto.getSubmissionType());
        task.setMaxSubmitCount(dto.getMaxSubmitCount());
        task.setMaxScore(dto.getTotalScore());
        task.setDescription(dto.getDescription());
        task.setRequirement(dto.getRequirement());
        task.setGradingRule(dto.getGradingRule());
        task.setEndTime(dto.getEndTime());
        task.setAllowLate(dto.getAllowLate() != null ? dto.getAllowLate() : 0);
        task.setStandardId(SystemConstants.DEFAULT_STANDARD_ID);
        task.setStatus("DRAFT");

        trainingTaskMapper.insert(task);

        if (dto.getTrainingClassId() != null) {
            TrainingClass tc = new TrainingClass();
            tc.setTrainingId(task.getId());
            tc.setClassId(dto.getTrainingClassId());
            trainingClassMapper.insert(tc);
        }

        Course course = courseMapper.selectById(courseId);

        TaskListVO vo = new TaskListVO();
        vo.setTaskId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setCourseName(course != null ? course.getCourseName() : "");
        vo.setSubmissionType(task.getSubmissionType());
        vo.setMaxSubmitCount(task.getMaxSubmitCount());
        vo.setTotalScore(task.getMaxScore());
        vo.setStatus(task.getStatus());
        vo.setDeadline(task.getEndTime());
        vo.setPublishTime(task.getPublishTime());
        vo.setSubmissionCount(0);
        vo.setReviewedCount(0);

        return vo;
    }

    @Override
    public TaskListVO updateTask(Long taskId, TaskUpdateDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该任务");
        }

        if (StringUtils.hasText(dto.getTaskName())) {
            task.setTaskName(dto.getTaskName());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            task.setDescription(dto.getDescription());
        }
        if (StringUtils.hasText(dto.getRequirement())) {
            task.setRequirement(dto.getRequirement());
        }
        if (StringUtils.hasText(dto.getSubmissionType())) {
            task.setSubmissionType(dto.getSubmissionType());
        }
        if (dto.getMaxSubmitCount() != null) {
            task.setMaxSubmitCount(dto.getMaxSubmitCount());
        }
        if (dto.getAllowLate() != null) {
            task.setAllowLate(dto.getAllowLate());
        }
        if (dto.getEndTime() != null) {
            task.setEndTime(dto.getEndTime());
        }
        if (dto.getTotalScore() != null) {
            task.setMaxScore(dto.getTotalScore());
        }
        if (dto.getStandardId() != null) {
            task.setStandardId(dto.getStandardId());
        }

        trainingTaskMapper.updateById(task);

        TrainingTask updated = trainingTaskMapper.selectById(taskId);
        Course course = courseMapper.selectById(updated.getCourseId());

        TaskListVO vo = new TaskListVO();
        vo.setTaskId(updated.getId());
        vo.setTaskName(updated.getTaskName());
        vo.setCourseName(course != null ? course.getCourseName() : "");
        vo.setSubmissionType(updated.getSubmissionType());
        vo.setMaxSubmitCount(updated.getMaxSubmitCount());
        vo.setTotalScore(updated.getMaxScore());
        vo.setStatus(updated.getStatus());
        vo.setDeadline(updated.getEndTime());
        vo.setPublishTime(updated.getPublishTime());

        List<Submission> submissions = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getTrainingTaskId, taskId)
                        .eq(Submission::getDeleted, 0));
        long submittedUserCount = submissions.stream()
                .map(Submission::getUserId).distinct().count();
        vo.setSubmissionCount((int) submittedUserCount);

        if (!submissions.isEmpty()) {
            List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
            long reviewedCount = teacherReviewMapper.selectCount(
                    new LambdaQueryWrapper<TeacherReview>()
                            .in(TeacherReview::getSubmissionId, submissionIds));
            vo.setReviewedCount((int) reviewedCount);
        } else {
            vo.setReviewedCount(0);
        }

        return vo;
    }

    @Override
    public PageResult<TaskListVO> listAllTasks(Long courseId, int page, int pageSize, String keyword, String status) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        List<Long> courseIds = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getUserId, teacherId))
                .stream().map(CourseTeacher::getCourseId).distinct().toList();

        if (courseIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        if (courseId != null) {
            if (!courseIds.contains(courseId)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该课程");
            }
            courseIds = List.of(courseId);
        }

        LambdaQueryWrapper<TrainingTask> query = new LambdaQueryWrapper<TrainingTask>()
                .in(TrainingTask::getCourseId, courseIds)
                .eq(TrainingTask::getDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            query.like(TrainingTask::getTaskName, keyword);
        }
        if (StringUtils.hasText(status)) {
            query.eq(TrainingTask::getStatus, status);
        }
        query.orderByDesc(TrainingTask::getCreateTime);

        IPage<TrainingTask> taskPage = trainingTaskMapper.selectPage(
                new Page<>(page, pageSize), query);

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c, (a, b) -> a));

        List<TaskListVO> vos = new ArrayList<>();
        for (TrainingTask task : taskPage.getRecords()) {
            TaskListVO vo = new TaskListVO();
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getTaskName());
            Course c = courseMap.get(task.getCourseId());
            vo.setCourseName(c != null ? c.getCourseName() : "");
            vo.setSubmissionType(task.getSubmissionType());
            vo.setMaxSubmitCount(task.getMaxSubmitCount());
            vo.setTotalScore(task.getMaxScore());
            vo.setStatus(task.getStatus());
            vo.setDeadline(task.getEndTime());
            vo.setPublishTime(task.getPublishTime());

            List<Submission> submissions = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .eq(Submission::getTrainingTaskId, task.getId())
                            .eq(Submission::getDeleted, 0));
            long submittedUserCount = submissions.stream()
                    .map(Submission::getUserId).distinct().count();
            vo.setSubmissionCount((int) submittedUserCount);

            if (!submissions.isEmpty()) {
                List<Long> submissionIds = submissions.stream().map(Submission::getId).toList();
                long reviewedCount = teacherReviewMapper.selectCount(
                        new LambdaQueryWrapper<TeacherReview>()
                                .in(TeacherReview::getSubmissionId, submissionIds));
                vo.setReviewedCount((int) reviewedCount);
            } else {
                vo.setReviewedCount(0);
            }
            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, taskPage.getTotal());
    }

    @Override
    public void deleteTask(Long taskId) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该任务");
        }

        task.setDeleted(1);
        trainingTaskMapper.updateById(task);
    }

    @Override
    public void publishTask(Long taskId) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        TrainingTask task = trainingTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }

        Long belongCount = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, task.getCourseId())
                        .eq(CourseTeacher::getUserId, teacherId));
        if (belongCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该任务");
        }

        if ("PUBLISHED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "任务已发布");
        }

        task.setStatus("PUBLISHED");
        task.setPublishTime(LocalDateTime.now());
        trainingTaskMapper.updateById(task);
    }
}

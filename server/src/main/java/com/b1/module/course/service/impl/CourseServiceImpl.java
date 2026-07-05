package com.b1.module.course.service.impl;

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
import com.b1.module.course.service.CourseService;
import com.b1.module.course.vo.CourseDetailVO;
import com.b1.module.course.vo.CourseVO;
import com.b1.module.score.entity.ScoreRecord;
import com.b1.module.score.mapper.ScoreRecordMapper;
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final UserMapper userMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;
    private final ScoreRecordMapper scoreRecordMapper;

    @Override
    public PageResult<CourseVO> listCourses(int page, int pageSize, String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();

        List<CourseStudent> courseStudents = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getUserId, userId));

        if (courseStudents.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        List<Long> courseIds = courseStudents.stream()
                .map(CourseStudent::getCourseId)
                .toList();

        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<Course>()
                .in(Course::getId, courseIds);
        if (StringUtils.hasText(keyword)) {
            courseQuery.like(Course::getCourseName, keyword);
        }

        IPage<Course> coursePage = courseMapper.selectPage(
                new Page<>(page, pageSize), courseQuery);

        List<Course> courses = coursePage.getRecords();

        Map<Long, String> teacherNameMap = buildTeacherNameMap(courseIds);

        List<CourseVO> vos = new ArrayList<>();
        for (Course course : courses) {
            CourseVO vo = new CourseVO();
            vo.setCourseId(course.getId());
            vo.setCourseName(course.getCourseName());
            vo.setCourseCode(course.getCourseCode());
            vo.setSemester(course.getSemester());
            vo.setCredits(course.getCredits());
            vo.setDescription(course.getDescription());
            vo.setTeacherName(teacherNameMap.getOrDefault(course.getId(), "待分配"));

            Long taskCount = trainingTaskMapper.selectCount(
                    new LambdaQueryWrapper<TrainingTask>()
                            .eq(TrainingTask::getCourseId, course.getId())
                            .eq(TrainingTask::getDeleted, 0));
            vo.setTaskCount(taskCount.intValue());

            vos.add(vo);
        }

        return PageResult.of(vos, page, pageSize, coursePage.getTotal());
    }

    @Override
    public CourseDetailVO getCourseDetail(Long courseId) {
        Long userId = StpUtil.getLoginIdAsLong();

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }

        CourseDetailVO vo = new CourseDetailVO();
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getCourseName());
        vo.setCourseCode(course.getCourseCode());
        vo.setSemester(course.getSemester());
        vo.setCredits(course.getCredits());
        vo.setDescription(course.getDescription());

        Map<Long, String> teacherNameMap = buildTeacherNameMap(List.of(courseId));
        vo.setTeacherName(teacherNameMap.getOrDefault(courseId, "待分配"));

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
        if (!courseTeachers.isEmpty()) {
            User teacher = userMapper.selectById(courseTeachers.get(0).getUserId());
            if (teacher != null) {
                vo.setTeacherEmail(teacher.getEmail());
            }
        }

        List<TrainingTask> tasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>()
                        .eq(TrainingTask::getCourseId, courseId)
                        .eq(TrainingTask::getStatus, "PUBLISHED")
                        .eq(TrainingTask::getDeleted, 0));

        List<CourseDetailVO.CourseTaskVO> taskVOs = new ArrayList<>();
        for (TrainingTask task : tasks) {
            CourseDetailVO.CourseTaskVO taskVO = new CourseDetailVO.CourseTaskVO();
            taskVO.setTaskId(task.getId());
            taskVO.setTaskName(task.getTaskName());
            taskVO.setDeadline(task.getEndTime());
            taskVO.setTotalScore(task.getMaxScore());

            List<Submission> submissions = submissionMapper.selectList(
                    new LambdaQueryWrapper<Submission>()
                            .eq(Submission::getUserId, userId)
                            .eq(Submission::getTrainingTaskId, task.getId())
                            .eq(Submission::getDeleted, 0)
                            .orderByDesc(Submission::getSubmitCount)
                            .last("LIMIT 1"));
            if (submissions.isEmpty()) {
                taskVO.setMySubmissionStatus("NOT_SUBMITTED");
            } else {
                taskVO.setMySubmissionStatus(submissions.get(0).getStatus());
            }

            List<ScoreRecord> scores = scoreRecordMapper.selectList(
                    new LambdaQueryWrapper<ScoreRecord>()
                            .eq(ScoreRecord::getUserId, userId)
                            .eq(ScoreRecord::getTrainingTaskId, task.getId()));
            if (!scores.isEmpty()) {
                taskVO.setMyScore(scores.get(0).getTotalScore());
            }

            taskVOs.add(taskVO);
        }
        vo.setTasks(taskVOs);

        return vo;
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
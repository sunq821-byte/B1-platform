package com.b1.module.teacher.service.impl;

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
import com.b1.module.submission.entity.Submission;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.entity.TrainingTask;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.dto.CourseCreateDTO;
import com.b1.module.teacher.dto.CourseUpdateDTO;
import com.b1.module.teacher.service.TeacherCourseService;
import com.b1.module.teacher.vo.CourseDetailVO;
import com.b1.module.teacher.vo.CourseVO;
import com.b1.module.teacher.vo.StudentVO;
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
public class TeacherCourseServiceImpl implements TeacherCourseService {

    private final CourseMapper courseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<CourseVO> listCourses(int page, int pageSize, String keyword) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getUserId, teacherId));

        if (courseTeachers.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        List<Long> courseIds = courseTeachers.stream()
                .map(CourseTeacher::getCourseId).toList();

        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<Course>()
                .in(Course::getId, courseIds);
        if (StringUtils.hasText(keyword)) {
            courseQuery.and(w -> w.like(Course::getCourseName, keyword)
                    .or().like(Course::getCourseCode, keyword));
        }

        IPage<Course> coursePage = courseMapper.selectPage(
                new Page<>(page, pageSize), courseQuery);

        List<CourseVO> vos = new ArrayList<>();
        for (Course course : coursePage.getRecords()) {
            CourseVO vo = new CourseVO();
            vo.setCourseId(course.getId());
            vo.setCourseName(course.getCourseName());
            vo.setCourseCode(course.getCourseCode());
            vo.setSemester(course.getSemester());
            vo.setCredits(course.getCredits());
            vo.setStatus(course.getStatus());
            vo.setCreateTime(course.getCreateTime());

            Long studentCount = courseStudentMapper.selectCount(
                    new LambdaQueryWrapper<CourseStudent>()
                            .eq(CourseStudent::getCourseId, course.getId()));
            vo.setStudentCount(studentCount.intValue());

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
        Long teacherId = StpUtil.getLoginIdAsLong();

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }

        Long count = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getUserId, teacherId));
        if (count == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该课程");
        }

        CourseDetailVO vo = new CourseDetailVO();
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getCourseName());
        vo.setCourseCode(course.getCourseCode());
        vo.setSemester(course.getSemester());
        vo.setCredits(course.getCredits());
        vo.setDescription(course.getDescription());
        vo.setSyllabus(course.getSyllabus());
        vo.setObjectives(course.getObjectives());
        vo.setStatus(course.getStatus());

        Long studentCount = courseStudentMapper.selectCount(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, courseId));
        vo.setStudentCount(studentCount.intValue());

        List<CourseTeacher> allTeachers = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId));
        List<Long> teacherIds = allTeachers.stream().map(CourseTeacher::getUserId).toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<CourseDetailVO.TeacherVO> teacherVOs = new ArrayList<>();
        for (CourseTeacher ct : allTeachers) {
            User u = userMap.get(ct.getUserId());
            if (u != null) {
                CourseDetailVO.TeacherVO tv = new CourseDetailVO.TeacherVO();
                tv.setUserId(u.getId());
                tv.setRealName(u.getRealName());
                tv.setEmail(u.getEmail());
                teacherVOs.add(tv);
            }
        }
        vo.setTeachers(teacherVOs);

        List<TrainingTask> tasks = trainingTaskMapper.selectList(
                new LambdaQueryWrapper<TrainingTask>()
                        .eq(TrainingTask::getCourseId, courseId)
                        .eq(TrainingTask::getDeleted, 0));

        List<CourseDetailVO.TaskSummaryVO> taskVOs = new ArrayList<>();
        for (TrainingTask task : tasks) {
            CourseDetailVO.TaskSummaryVO ts = new CourseDetailVO.TaskSummaryVO();
            ts.setTaskId(task.getId());
            ts.setTaskName(task.getTaskName());
            ts.setStatus(task.getStatus());
            ts.setDeadline(task.getEndTime());

            Long subCount = submissionMapper.selectCount(
                    new LambdaQueryWrapper<Submission>()
                            .eq(Submission::getTrainingTaskId, task.getId())
                            .eq(Submission::getDeleted, 0));
            ts.setSubmissionCount(subCount.intValue());

            taskVOs.add(ts);
        }
        vo.setTasks(taskVOs);

        return vo;
    }

    @Override
    public CourseVO createCourse(CourseCreateDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Long existing = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getCourseCode, dto.getCourseCode()));
        if (existing > 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "课程编码已存在");
        }

        Course course = new Course();
        course.setCourseName(dto.getCourseName());
        course.setCourseCode(dto.getCourseCode());
        course.setSemester(dto.getSemester());
        course.setCredits(dto.getCredits());
        course.setDescription(dto.getDescription());
        course.setSyllabus(dto.getSyllabus());
        course.setObjectives(dto.getObjectives());
        course.setStatus("DRAFT");

        courseMapper.insert(course);

        CourseTeacher ct = new CourseTeacher();
        ct.setCourseId(course.getId());
        ct.setUserId(teacherId);
        courseTeacherMapper.insert(ct);

        CourseVO vo = new CourseVO();
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getCourseName());
        vo.setCourseCode(course.getCourseCode());
        vo.setSemester(course.getSemester());
        vo.setCredits(course.getCredits());
        vo.setStatus(course.getStatus());
        vo.setCreateTime(course.getCreateTime());
        vo.setStudentCount(0);
        vo.setTaskCount(0);

        return vo;
    }

    @Override
    public CourseVO updateCourse(Long courseId, CourseUpdateDTO dto) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }

        Long count = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getUserId, teacherId));
        if (count == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权操作该课程");
        }

        if (StringUtils.hasText(dto.getCourseName())) {
            course.setCourseName(dto.getCourseName());
        }
        if (StringUtils.hasText(dto.getCourseCode())) {
            course.setCourseCode(dto.getCourseCode());
        }
        if (StringUtils.hasText(dto.getSemester())) {
            course.setSemester(dto.getSemester());
        }
        if (dto.getCredits() != null) {
            course.setCredits(dto.getCredits());
        }
        if (dto.getDescription() != null) {
            course.setDescription(dto.getDescription());
        }
        if (dto.getSyllabus() != null) {
            course.setSyllabus(dto.getSyllabus());
        }
        if (dto.getObjectives() != null) {
            course.setObjectives(dto.getObjectives());
        }

        courseMapper.updateById(course);

        Course updated = courseMapper.selectById(courseId);

        CourseVO vo = new CourseVO();
        vo.setCourseId(updated.getId());
        vo.setCourseName(updated.getCourseName());
        vo.setCourseCode(updated.getCourseCode());
        vo.setSemester(updated.getSemester());
        vo.setCredits(updated.getCredits());
        vo.setStatus(updated.getStatus());
        vo.setCreateTime(updated.getCreateTime());

        Long studentCount = courseStudentMapper.selectCount(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, courseId));
        vo.setStudentCount(studentCount.intValue());

        Long taskCount = trainingTaskMapper.selectCount(
                new LambdaQueryWrapper<TrainingTask>()
                        .eq(TrainingTask::getCourseId, courseId)
                        .eq(TrainingTask::getDeleted, 0));
        vo.setTaskCount(taskCount.intValue());

        return vo;
    }

    @Override
    public PageResult<StudentVO> listCourseStudents(Long courseId, int page, int pageSize) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }

        Long count = courseTeacherMapper.selectCount(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getUserId, teacherId));
        if (count == 0) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权访问该课程");
        }

        IPage<CourseStudent> studentPage = courseStudentMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, courseId));

        List<Long> userIds = studentPage.getRecords().stream()
                .map(CourseStudent::getUserId).toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<StudentVO> vos = new ArrayList<>();
        for (CourseStudent cs : studentPage.getRecords()) {
            User u = userMap.get(cs.getUserId());
            if (u == null) continue;

            StudentVO sv = new StudentVO();
            sv.setUserId(u.getId());
            sv.setRealName(u.getRealName());
            sv.setEmail(u.getEmail());
            sv.setPhone(u.getPhone());

            List<Long> courseTaskIds = trainingTaskMapper.selectList(
                    new LambdaQueryWrapper<TrainingTask>()
                            .select(TrainingTask::getId)
                            .eq(TrainingTask::getCourseId, courseId))
                    .stream().map(TrainingTask::getId).toList();

            Long subCount = 0L;
            if (!courseTaskIds.isEmpty()) {
                subCount = submissionMapper.selectCount(
                        new LambdaQueryWrapper<Submission>()
                                .eq(Submission::getUserId, cs.getUserId())
                                .eq(Submission::getDeleted, 0)
                                .in(Submission::getTrainingTaskId, courseTaskIds));
            }
            sv.setSubmissionCount(subCount.intValue());

            vos.add(sv);
        }

        return PageResult.of(vos, page, pageSize, studentPage.getTotal());
    }
}

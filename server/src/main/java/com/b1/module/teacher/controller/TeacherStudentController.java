package com.b1.module.teacher.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.course.entity.CourseStudent;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseStudentMapper;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.submission.mapper.SubmissionMapper;
import com.b1.module.task.mapper.TrainingTaskMapper;
import com.b1.module.teacher.vo.StudentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "教师-学生管理")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherStudentController {

    private final CourseTeacherMapper courseTeacherMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final UserMapper userMapper;
    private final TrainingTaskMapper trainingTaskMapper;
    private final SubmissionMapper submissionMapper;

    @Operation(summary = "学生列表")
    @GetMapping("/students")
    public PageResult<StudentVO> listStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Long teacherId = StpUtil.getLoginIdAsLong();

        List<Long> courseIds = courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getUserId, teacherId))
                .stream().map(CourseTeacher::getCourseId).distinct().toList();

        if (courseIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        List<Long> userIds = courseStudentMapper.selectList(
                new LambdaQueryWrapper<CourseStudent>()
                        .in(CourseStudent::getCourseId, courseIds))
                .stream().map(CourseStudent::getUserId).distinct().toList();

        if (userIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page, pageSize, 0);
        }

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<StudentVO> vos = new ArrayList<>();
        for (Map.Entry<Long, User> entry : userMap.entrySet()) {
            User u = entry.getValue();

            // Filter by keyword if provided
            if (keyword != null && !keyword.isEmpty() &&
                    !u.getRealName().toLowerCase().contains(keyword.toLowerCase()) &&
                    !u.getUsername().toLowerCase().contains(keyword.toLowerCase())) {
                continue;
            }

            StudentVO sv = new StudentVO();
            sv.setUserId(u.getId());
            sv.setRealName(u.getRealName());
            sv.setEmail(u.getEmail());
            sv.setPhone(u.getPhone());
            vos.add(sv);
        }

        // Simple pagination in-memory
        int total = vos.size();
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= total) {
            return PageResult.of(Collections.emptyList(), page, pageSize, total);
        }
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<StudentVO> paged = vos.subList(fromIndex, toIndex);

        return PageResult.of(paged, page, pageSize, total);
    }

    @Operation(summary = "学生详情")
    @GetMapping("/students/{userId}")
    public Result<StudentVO> getStudentDetail(@PathVariable Long userId) {
        User u = userMapper.selectById(userId);
        if (u == null) {
            return Result.ok(null);
        }
        StudentVO sv = new StudentVO();
        sv.setUserId(u.getId());
        sv.setRealName(u.getRealName());
        sv.setEmail(u.getEmail());
        sv.setPhone(u.getPhone());
        return Result.ok(sv);
    }
}

package com.b1.module.teacher.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.teacher.dto.CourseCreateDTO;
import com.b1.module.teacher.dto.CourseUpdateDTO;
import com.b1.module.teacher.service.TeacherCourseService;
import com.b1.module.teacher.vo.CourseDetailVO;
import com.b1.module.teacher.vo.CourseVO;
import com.b1.module.teacher.vo.StudentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "教师-课程管理")
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final TeacherCourseService teacherCourseService;

    @Operation(summary = "课程列表")
    @GetMapping("/courses")
    public PageResult<CourseVO> listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return teacherCourseService.listCourses(page, pageSize, keyword);
    }

    @Operation(summary = "课程详情")
    @GetMapping("/courses/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        return Result.ok(teacherCourseService.getCourseDetail(courseId));
    }

    @Operation(summary = "创建课程")
    @PostMapping("/courses")
    public Result<CourseVO> createCourse(@Valid @RequestBody CourseCreateDTO dto) {
        return Result.ok(teacherCourseService.createCourse(dto));
    }

    @Operation(summary = "更新课程")
    @PutMapping("/courses/{courseId}")
    public Result<CourseVO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateDTO dto) {
        return Result.ok(teacherCourseService.updateCourse(courseId, dto));
    }

    @Operation(summary = "课程学生列表")
    @GetMapping("/courses/{courseId}/students")
    public PageResult<StudentVO> listCourseStudents(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return teacherCourseService.listCourseStudents(courseId, page, pageSize);
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/courses/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        teacherCourseService.deleteCourse(courseId);
        return Result.ok();
    }
}

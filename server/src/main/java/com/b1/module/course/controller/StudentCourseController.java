package com.b1.module.course.controller;

import com.b1.common.result.PageResult;
import com.b1.common.result.Result;
import com.b1.module.course.service.CourseService;
import com.b1.module.course.vo.CourseDetailVO;
import com.b1.module.course.vo.CourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学生-课程")
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseService courseService;

    @Operation(summary = "学生课程列表")
    @GetMapping("/courses")
    public Result<PageResult<CourseVO>> listCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(courseService.listCourses(page, pageSize, keyword));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/courses/{courseId}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long courseId) {
        return Result.ok(courseService.getCourseDetail(courseId));
    }
}
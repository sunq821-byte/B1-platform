package com.b1.module.teacher.service;

import com.b1.common.result.PageResult;
import com.b1.module.teacher.dto.CourseCreateDTO;
import com.b1.module.teacher.dto.CourseUpdateDTO;
import com.b1.module.teacher.vo.CourseDetailVO;
import com.b1.module.teacher.vo.CourseVO;
import com.b1.module.teacher.vo.StudentVO;

public interface TeacherCourseService {

    PageResult<CourseVO> listCourses(int page, int pageSize, String keyword);

    CourseDetailVO getCourseDetail(Long courseId);

    CourseVO createCourse(CourseCreateDTO dto);

    CourseVO updateCourse(Long courseId, CourseUpdateDTO dto);

    void deleteCourse(Long courseId);

    PageResult<StudentVO> listCourseStudents(Long courseId, int page, int pageSize);
}

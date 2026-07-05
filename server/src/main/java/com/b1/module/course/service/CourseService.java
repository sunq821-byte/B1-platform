package com.b1.module.course.service;

import com.b1.common.result.PageResult;
import com.b1.module.course.vo.CourseDetailVO;
import com.b1.module.course.vo.CourseVO;

public interface CourseService {

    PageResult<CourseVO> listCourses(int page, int pageSize, String keyword);

    CourseDetailVO getCourseDetail(Long courseId);
}
package com.b1.module.teacher.service;

import com.b1.module.teacher.vo.ClassReportVO;
import com.b1.module.teacher.vo.CollegeReportVO;

public interface TeacherReportService {

    ClassReportVO getClassReport(String className);

    CollegeReportVO getCollegeReport();
}

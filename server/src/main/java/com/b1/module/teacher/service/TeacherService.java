package com.b1.module.teacher.service;

import com.b1.module.teacher.vo.TeacherDashboardVO;

import java.util.List;

public interface TeacherService {

    TeacherDashboardVO getDashboard();

    /**
     * 向所选实训任务中"未提交"的学生发送催交站内通知。
     * 仅处理当前教师有权（任课）的任务，返回实际发送的通知条数。
     */
    int remindUnsubmitted(List<Long> taskIds);
}

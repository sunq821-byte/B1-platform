package com.b1.module.student.service;

import com.b1.common.result.PageResult;
import com.b1.module.student.vo.DashboardVO;
import com.b1.module.student.vo.GrowthProfileVO;
import com.b1.module.student.vo.NotificationVO;
import com.b1.module.student.vo.StudentReportVO;

public interface StudentService {

    DashboardVO getDashboard();

    PageResult<NotificationVO> listNotifications(int page, int pageSize);

    void markNotificationRead(Long notificationId);

    void markAllNotificationsRead();

    GrowthProfileVO getGrowthProfile();

    StudentReportVO getStudentReport();
}

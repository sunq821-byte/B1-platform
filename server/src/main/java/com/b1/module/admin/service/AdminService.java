package com.b1.module.admin.service;

import com.b1.module.admin.dto.AdminClassFormDTO;
import com.b1.module.admin.dto.AdminUserFormDTO;
import com.b1.module.admin.vo.*;

import java.util.List;

public interface AdminService {

    AdminDashboardVO getDashboard();

    List<AdminUserVO> listUsers(String role, String keyword);

    AdminUserVO createUser(AdminUserFormDTO dto);

    AdminUserVO toggleUserStatus(Long userId);

    void updateUser(Long userId, AdminUserFormDTO dto);

    List<AdminClassVO> listClasses();

    AdminClassVO createClass(AdminClassFormDTO dto);

    void updateClass(Long classId, AdminClassFormDTO dto);

    void deleteClass(Long classId);

    AdminSystemConfigVO getSystemConfig();

    void saveSystemConfig(AdminSystemConfigVO config);

    List<AdminLogVO> getLogs(String type);

    AdminMonitorVO getMonitor();
}

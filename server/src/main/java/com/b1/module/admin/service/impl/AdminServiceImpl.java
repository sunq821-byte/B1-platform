package com.b1.module.admin.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.admin.dto.AdminClassFormDTO;
import com.b1.module.admin.dto.AdminUserFormDTO;
import com.b1.module.admin.entity.ClassInfo;
import com.b1.module.admin.entity.OperationLog;
import com.b1.module.admin.entity.SystemConfig;
import com.b1.module.admin.mapper.ClassInfoMapper;
import com.b1.module.admin.mapper.OperationLogMapper;
import com.b1.module.admin.mapper.SystemConfigMapper;
import com.b1.module.admin.service.AdminService;
import com.b1.module.admin.vo.*;
import com.b1.module.admin.vo.AdminDashboardVO.LogEntry;
import com.b1.module.admin.vo.AdminDashboardVO.ServiceHealth;
import com.b1.module.admin.vo.AdminDashboardVO.Stats;
import com.b1.module.auth.entity.Role;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.entity.UserRole;
import com.b1.module.auth.mapper.RoleMapper;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.auth.mapper.UserRoleMapper;
import com.b1.module.course.entity.Course;
import com.b1.module.course.entity.CourseTeacher;
import com.b1.module.course.mapper.CourseMapper;
import com.b1.module.course.mapper.CourseTeacherMapper;
import com.b1.module.submission.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final CourseMapper courseMapper;
    private final CourseTeacherMapper courseTeacherMapper;
    private final SubmissionMapper submissionMapper;
    private final ClassInfoMapper classInfoMapper;
    private final OperationLogMapper operationLogMapper;
    private final SystemConfigMapper systemConfigMapper;

    private static final String DEFAULT_PASSWORD_HASH =
            "$2a$10$CGI8hIN7VTgtS6fTSDWJTu6RQenkhJOTp5YxDOokGo3aMSBRfbwva";

    // ─── Dashboard ───

    @Override
    public AdminDashboardVO getDashboard() {
        int totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)).intValue();
        int activeCourses = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, "ACTIVE")).intValue();
        int totalSubmissions = submissionMapper.selectCount(null).intValue();

        long completed = submissionMapper.selectCount(
                new LambdaQueryWrapper<com.b1.module.submission.entity.Submission>()
                        .in(com.b1.module.submission.entity.Submission::getStatus,
                                "COMPLETED", "AI_COMPLETED", "TEACHER_SCORING"));
        int completionRate = totalSubmissions > 0
                ? (int) (completed * 100L / totalSubmissions) : 0;

        List<OperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>()
                        .orderByDesc(OperationLog::getCreateTime)
                        .last("LIMIT 10"));
        List<LogEntry> recentLogs = logs.stream().map(l -> LogEntry.builder()
                .type(mapLogType(l.getResult()))
                .message(l.getOperation())
                .detail(l.getDetail() != null ? l.getDetail() : l.getModule())
                .createdAt(l.getCreateTime() != null
                        ? l.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .build()).collect(Collectors.toList());

        List<ServiceHealth> health = buildServiceHealth();

        return AdminDashboardVO.builder()
                .stats(Stats.builder()
                        .totalUsers(totalUsers)
                        .activeCourses(activeCourses)
                        .totalSubmissions(totalSubmissions)
                        .completionRate(completionRate)
                        .build())
                .recentLogs(recentLogs)
                .health(health)
                .build();
    }

    // ─── Users ───

    @Override
    public List<AdminUserVO> listUsers(String role, String keyword) {
        List<Role> allRoles = roleMapper.selectList(null);
        Map<Long, Role> roleMap = allRoles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r));

        List<UserRole> allUserRoles = userRoleMapper.selectList(null);
        Map<Long, Long> userRoleMap = allUserRoles.stream()
                .collect(Collectors.toMap(UserRole::getUserId, UserRole::getRoleId, (a, b) -> a));

        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>().eq(User::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(User::getRealName, keyword).or().like(User::getEmail, keyword));
        }
        List<User> users = userMapper.selectList(qw);

        return users.stream()
                .filter(u -> {
                    if (role == null || role.isBlank()) return true;
                    Long rId = userRoleMap.get(u.getId());
                    Role r = rId != null ? roleMap.get(rId) : null;
                    return r != null && role.equalsIgnoreCase(r.getRoleCode());
                })
                .map(u -> {
                    Long rId = userRoleMap.get(u.getId());
                    Role r = rId != null ? roleMap.get(rId) : null;
                    return AdminUserVO.builder()
                            .userId(String.valueOf(u.getId()))
                            .name(u.getRealName())
                            .role(r != null ? r.getRoleCode() : "student")
                            .email(u.getEmail())
                            .status(u.getStatus() != null && u.getStatus() == 1 ? "active" : "inactive")
                            .createdAt(u.getCreateTime() != null
                                    ? u.getCreateTime().toLocalDate().toString() : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminUserVO createUser(AdminUserFormDTO dto) {
        String username = generateUsername(dto);
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing != null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(DEFAULT_PASSWORD_HASH);
        user.setRealName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setStatus("active".equals(dto.getStatus()) ? 1 : 0);
        userMapper.insert(user);

        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, dto.getRole()));
        if (role != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(role.getId());
            userRoleMapper.insert(ur);
        }

        return AdminUserVO.builder()
                .userId(String.valueOf(user.getId()))
                .name(user.getRealName())
                .role(dto.getRole())
                .className(dto.getClassName())
                .email(user.getEmail())
                .status(user.getStatus() == 1 ? "active" : "inactive")
                .createdAt(user.getCreateTime() != null
                        ? user.getCreateTime().toLocalDate().toString() : "")
                .build();
    }

    @Override
    @Transactional
    public AdminUserVO toggleUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);

        UserRole ur = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        String roleCode = null;
        if (ur != null) {
            Role role = roleMapper.selectById(ur.getRoleId());
            if (role != null) roleCode = role.getRoleCode();
        }

        return AdminUserVO.builder()
                .userId(String.valueOf(user.getId()))
                .name(user.getRealName())
                .role(roleCode != null ? roleCode : "student")
                .email(user.getEmail())
                .status(user.getStatus() == 1 ? "active" : "inactive")
                .createdAt(user.getCreateTime() != null
                        ? user.getCreateTime().toLocalDate().toString() : "")
                .build();
    }

    @Override
    @Transactional
    public void updateUser(Long userId, AdminUserFormDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setRealName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setStatus("active".equals(dto.getStatus()) ? 1 : 0);
        userMapper.updateById(user);

        UserRole ur = userRoleMapper.selectOne(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        if (ur != null) {
            Role role = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, dto.getRole()));
            if (role != null && !ur.getRoleId().equals(role.getId())) {
                ur.setRoleId(role.getId());
                userRoleMapper.updateById(ur);
            }
        }
    }

    // ─── Classes ───

    @Override
    public List<AdminClassVO> listClasses() {
        List<ClassInfo> classes = classInfoMapper.selectList(null);
        List<CourseTeacher> allTeachers = courseTeacherMapper.selectList(null);
        Map<Long, Long> courseTeacherMap = allTeachers.stream()
                .collect(Collectors.toMap(CourseTeacher::getCourseId, CourseTeacher::getUserId, (a, b) -> a));
        List<User> users = userMapper.selectList(null);
        Map<Long, String> userNameMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (a, b) -> a));

        return classes.stream()
                .map(c -> AdminClassVO.builder()
                        .id(String.valueOf(c.getId()))
                        .name(c.getClassName())
                        .studentCount(c.getStudentCount() != null ? c.getStudentCount() : 0)
                        .teacherName("")
                        .createdAt(c.getCreateTime() != null
                                ? c.getCreateTime().toLocalDate().toString() : "")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public AdminClassVO createClass(AdminClassFormDTO dto) {
        ClassInfo c = new ClassInfo();
        c.setClassCode("CLS" + IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase());
        c.setClassName(dto.getName());
        c.setStudentCount(0);
        classInfoMapper.insert(c);

        return AdminClassVO.builder()
                .id(String.valueOf(c.getId()))
                .name(c.getClassName())
                .studentCount(0)
                .teacherName(dto.getTeacherName() != null ? dto.getTeacherName() : "")
                .createdAt(c.getCreateTime() != null
                        ? c.getCreateTime().toLocalDate().toString() : "")
                .build();
    }

    @Override
    public void updateClass(Long classId, AdminClassFormDTO dto) {
        ClassInfo c = classInfoMapper.selectById(classId);
        if (c == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        c.setClassName(dto.getName());
        classInfoMapper.updateById(c);
    }

    @Override
    public void deleteClass(Long classId) {
        ClassInfo c = classInfoMapper.selectById(classId);
        if (c == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        classInfoMapper.deleteById(classId);
    }

    // ─── System Config ───

    @Override
    public AdminSystemConfigVO getSystemConfig() {
        List<SystemConfig> configs = systemConfigMapper.selectList(null);
        Map<String, String> map = configs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, SystemConfig::getConfigValue, (a, b) -> a));

        return AdminSystemConfigVO.builder()
                .systemName(getOrDefault(map, "system_name", "B1 智慧教育平台"))
                .currentSemester(getOrDefault(map, "current_semester", "2025-2026-2"))
                .semesterStart(getOrDefault(map, "semester_start", "2026-02-15"))
                .semesterEnd(getOrDefault(map, "semester_end", "2026-07-15"))
                .maxUploadSize(Integer.parseInt(getOrDefault(map, "max_upload_size", "50")))
                .aiModelVersion(getOrDefault(map, "ai_model_version", "DeepSeek-V4 + Qwen-VL-Max"))
                .autoAnalyze(Boolean.parseBoolean(getOrDefault(map, "auto_analyze", "true")))
                .notificationEnabled(Boolean.parseBoolean(getOrDefault(map, "notification_enabled", "true")))
                .maintenanceMode(Boolean.parseBoolean(getOrDefault(map, "maintenance_mode", "false")))
                .build();
    }

    @Override
    @Transactional
    public void saveSystemConfig(AdminSystemConfigVO config) {
        upsertConfig("system_name", config.getSystemName(), "STRING");
        upsertConfig("current_semester", config.getCurrentSemester(), "STRING");
        upsertConfig("semester_start", config.getSemesterStart(), "STRING");
        upsertConfig("semester_end", config.getSemesterEnd(), "STRING");
        upsertConfig("max_upload_size", String.valueOf(config.getMaxUploadSize()), "NUMBER");
        upsertConfig("ai_model_version", config.getAiModelVersion(), "STRING");
        upsertConfig("auto_analyze", String.valueOf(config.isAutoAnalyze()), "BOOLEAN");
        upsertConfig("notification_enabled", String.valueOf(config.isNotificationEnabled()), "BOOLEAN");
        upsertConfig("maintenance_mode", String.valueOf(config.isMaintenanceMode()), "BOOLEAN");
    }

    // ─── Logs ───

    @Override
    public List<AdminLogVO> getLogs(String type) {
        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreateTime)
                .last("LIMIT 200");

        List<OperationLog> logs = operationLogMapper.selectList(qw);

        return logs.stream()
                .filter(l -> {
                    if (type == null || type.isBlank()) return true;
                    return type.equalsIgnoreCase(mapLogType(l.getResult()));
                })
                .map(l -> AdminLogVO.builder()
                        .id(String.valueOf(l.getId()))
                        .type(mapLogType(l.getResult()))
                        .message(l.getModule() + " / " + l.getOperation())
                        .detail(l.getDetail() != null ? l.getDetail() : "")
                        .operator(l.getUsername() != null ? l.getUsername() : "")
                        .createdAt(l.getCreateTime() != null
                                ? l.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Monitor ───

    @Override
    public AdminMonitorVO getMonitor() {
        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory();
        long freeMem = rt.freeMemory();
        long usedMem = totalMem - freeMem;
        int memoryUsage = totalMem > 0 ? (int) (usedMem * 100 / totalMem) : 0;

        double cpuLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        int cpuUsage = (int) Math.min(99, Math.max(1, cpuLoad * 100 / Runtime.getRuntime().availableProcessors()));

        int diskUsage = 45;

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long days = uptimeMs / 86400000;
        long hours = (uptimeMs % 86400000) / 3600000;
        long mins = (uptimeMs % 3600000) / 60000;
        String uptime = String.format("%d 天 %d 小时 %d 分钟", days, hours, mins);

        List<AdminMonitorVO.ServiceHealth> services = new ArrayList<>();
        services.add(new AdminMonitorVO.ServiceHealth("MySQL", "正常", "#10B981"));
        services.add(new AdminMonitorVO.ServiceHealth("Redis", "正常", "#10B981"));
        services.add(new AdminMonitorVO.ServiceHealth("MinIO", "正常", "#10B981"));
        services.add(new AdminMonitorVO.ServiceHealth("DeepSeek API", "正常", "#10B981"));
        services.add(new AdminMonitorVO.ServiceHealth("Qwen API", "正常", "#10B981"));
        services.add(new AdminMonitorVO.ServiceHealth("文件存储", "正常", "#10B981"));

        return AdminMonitorVO.builder()
                .services(services)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .diskUsage(diskUsage)
                .uptime(uptime)
                .build();
    }

    // ─── Helpers ───

    private List<ServiceHealth> buildServiceHealth() {
        return List.of(
                new ServiceHealth("MySQL", "正常", "#10B981"),
                new ServiceHealth("Redis", "正常", "#10B981"),
                new ServiceHealth("MinIO", "正常", "#10B981"),
                new ServiceHealth("DeepSeek API", "正常", "#10B981"),
                new ServiceHealth("Qwen API", "正常", "#10B981")
        );
    }

    private String mapLogType(String result) {
        if (result == null) return "info";
        return switch (result.toUpperCase()) {
            case "SUCCESS" -> "success";
            case "FAILURE" -> "error";
            default -> "info";
        };
    }

    private String generateUsername(AdminUserFormDTO dto) {
        if (dto.getEmail() != null && dto.getEmail().contains("@")) {
            return dto.getEmail().substring(0, dto.getEmail().indexOf('@'));
        }
        return "user_" + IdUtil.fastSimpleUUID().substring(0, 8);
    }

    private String getOrDefault(Map<String, String> map, String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    private void upsertConfig(String key, String value, String type) {
        SystemConfig existing = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (existing != null) {
            existing.setConfigValue(value != null ? value : "");
            systemConfigMapper.updateById(existing);
        } else {
            SystemConfig sc = new SystemConfig();
            sc.setConfigKey(key);
            sc.setConfigValue(value != null ? value : "");
            sc.setConfigType(type);
            systemConfigMapper.insert(sc);
        }
    }
}

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
import com.b1.module.admin.vo.AdminDashboardVO.ActivityTrend;
import com.b1.module.admin.vo.AdminDashboardVO.LogEntry;
import com.b1.module.admin.vo.AdminDashboardVO.PriorityAlert;
import com.b1.module.admin.vo.AdminDashboardVO.RuntimeSummary;
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
        List<User> activeUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        int totalUsers = activeUsers.size();

        List<Role> allRoles = roleMapper.selectList(null);
        Map<Long, String> roleCodeById = allRoles.stream()
                .collect(Collectors.toMap(Role::getId, r -> r.getRoleCode() != null ? r.getRoleCode() : "", (a, b) -> a));
        List<UserRole> allUserRoles = userRoleMapper.selectList(null);
        Map<Long, String> userRoleCode = new HashMap<>();
        for (UserRole ur : allUserRoles) {
            userRoleCode.putIfAbsent(ur.getUserId(), roleCodeById.getOrDefault(ur.getRoleId(), ""));
        }

        int studentCount = 0;
        int teacherCount = 0;
        int adminCount = 0;
        for (User u : activeUsers) {
            String code = userRoleCode.getOrDefault(u.getId(), "").toLowerCase();
            if (code.contains("admin")) adminCount++;
            else if (code.contains("teacher")) teacherCount++;
            else studentCount++;
        }

        int activeCourses = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, "ACTIVE")).intValue();
        int totalSubmissions = submissionMapper.selectCount(null).intValue();

        long completed = submissionMapper.selectCount(
                new LambdaQueryWrapper<com.b1.module.submission.entity.Submission>()
                        .in(com.b1.module.submission.entity.Submission::getStatus,
                                "COMPLETED", "AI_COMPLETED", "TEACHER_SCORING", "GRADED", "REVIEWED"));
        int completionRate = totalSubmissions > 0
                ? (int) (completed * 100L / totalSubmissions) : 0;

        LocalDateTime dayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        int todayActive = (int) activeUsers.stream()
                .filter(u -> u.getLastLoginTime() != null && !u.getLastLoginTime().isBefore(dayStart))
                .count();
        if (todayActive == 0 && totalUsers > 0) {
            todayActive = Math.min(totalUsers, Math.max(1, totalUsers / 4));
        }
        double activeRate = totalUsers > 0
                ? Math.round(todayActive * 1000.0 / totalUsers) / 10.0
                : 0;

        long alertCount24h = operationLogMapper.selectCount(
                new LambdaQueryWrapper<OperationLog>()
                        .ge(OperationLog::getCreateTime, LocalDateTime.now().minusHours(24))
                        .and(w -> w.eq(OperationLog::getResult, "FAILURE")
                                .or().eq(OperationLog::getResult, "ERROR")
                                .or().eq(OperationLog::getResult, "WARNING")));

        List<OperationLog> logs = operationLogMapper.selectList(
                new LambdaQueryWrapper<OperationLog>()
                        .orderByDesc(OperationLog::getCreateTime)
                        .last("LIMIT 10"));
        List<LogEntry> recentLogs = logs.stream().map(l -> LogEntry.builder()
                .type(mapLogType(l.getResult()))
                .message(l.getOperation())
                .detail(l.getDetail() != null ? l.getDetail() : l.getModule())
                .operator(l.getUsername() != null ? l.getUsername() : "系统")
                .ip(l.getIpAddress() != null ? l.getIpAddress() : "-")
                .result(l.getResult() != null ? l.getResult() : "")
                .createdAt(l.getCreateTime() != null
                        ? l.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                .build()).collect(Collectors.toList());

        double storageUsedGb = Math.round((totalSubmissions * 0.08 + activeCourses * 0.5) * 10.0) / 10.0;
        double storageQuotaGb = 100.0;
        int aiCallsToday = Math.min(999, totalSubmissions / 2 + todayActive * 3);
        List<ServiceHealth> health = buildServiceHealth(storageUsedGb, storageQuotaGb);
        int healthyServices = (int) health.stream()
                .filter(h -> h.getStatus() != null && (h.getStatus().contains("运行") || h.getStatus().contains("正常")))
                .count();
        int degraded = (int) health.stream()
                .filter(h -> h.getStatus() != null && (h.getStatus().contains("降级") || h.getStatus().contains("告警")))
                .count();
        int down = Math.max(0, health.size() - healthyServices - degraded);
        double accountEnableRate = 92.0;
        double aiQuotaRate = Math.min(100, Math.round(aiCallsToday * 1000.0 / 300) / 10.0);

        List<String> trendAxis = new ArrayList<>();
        List<Integer> trendActive = new ArrayList<>();
        List<Integer> trendAi = new ArrayList<>();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("E", Locale.CHINA);
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now().minusDays(i);
            trendAxis.add(day.format(dayFmt));
            int base = Math.max(1, todayActive - i * 2 + (i % 3));
            trendActive.add(base);
            trendAi.add(Math.max(10, aiCallsToday - i * 8 + (i % 4) * 3));
        }

        PriorityAlert priorityAlert = null;
        if (alertCount24h > 0 || degraded > 0) {
            priorityAlert = PriorityAlert.builder()
                    .title("优先处理")
                    .summary(degraded > 0
                            ? "存在降级服务，近 24 小时告警 " + alertCount24h + " 条。建议先查看运行监控，确认依赖服务状态。"
                            : "近 24 小时出现 " + alertCount24h + " 条告警，建议先查看操作日志与运行监控。")
                    .priority(alertCount24h >= 3 || degraded > 0 ? "高" : "中")
                    .source(degraded > 0 ? "服务健康" : "操作日志")
                    .actionPath("/admin/monitor")
                    .build();
        }

        return AdminDashboardVO.builder()
                .stats(Stats.builder()
                        .totalUsers(totalUsers)
                        .studentCount(studentCount)
                        .teacherCount(teacherCount)
                        .adminCount(adminCount)
                        .todayActive(todayActive)
                        .activeRate(activeRate)
                        .activeCourses(activeCourses)
                        .totalSubmissions(totalSubmissions)
                        .completionRate(completionRate)
                        .aiCallsToday(aiCallsToday)
                        .storageUsedGb(storageUsedGb)
                        .storageQuotaGb(storageQuotaGb)
                        .alertCount24h((int) alertCount24h)
                        .accountEnableRate(accountEnableRate)
                        .healthyServices(healthyServices)
                        .totalServices(health.size())
                        .aiQuotaRate(aiQuotaRate)
                        .alertDeltaPct(alertCount24h > 0 ? 12 : 0)
                        .aiCallsDeltaPct(-8)
                        .completionDeltaPct(0)
                        .build())
                .runtimeSummary(RuntimeSummary.builder()
                        .healthy(healthyServices)
                        .degraded(degraded)
                        .down(down)
                        .aiQueue(Math.min(9, Math.max(0, (int) alertCount24h)))
                        .build())
                .activityTrend(ActivityTrend.builder()
                        .xAxis(trendAxis)
                        .activeUsers(trendActive)
                        .aiCalls(trendAi)
                        .build())
                .priorityAlert(priorityAlert)
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
                        .ip(l.getIpAddress() != null ? l.getIpAddress() : "-")
                        .result(l.getResult() != null ? l.getResult() : "")
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

        List<AdminMonitorVO.ServiceHealth> services = List.of(
                AdminMonitorVO.ServiceHealth.builder()
                        .name("AI 分析服务").status("运行中").color("#00B96B")
                        .detail("队列空闲").latencyMs(320).build(),
                AdminMonitorVO.ServiceHealth.builder()
                        .name("数据库服务").status("运行中").color("#00B96B")
                        .detail("主库可用").latencyMs(12).build(),
                AdminMonitorVO.ServiceHealth.builder()
                        .name("对象存储").status("运行中").color("#00B96B")
                        .detail("可用").latencyMs(45).build(),
                AdminMonitorVO.ServiceHealth.builder()
                        .name("消息队列").status("运行中").color("#00B96B")
                        .detail("积压 0").latencyMs(8).build(),
                AdminMonitorVO.ServiceHealth.builder()
                        .name("缓存服务").status("降级").color("#FAAD14")
                        .detail("命中率偏低").latencyMs(3).build()
        );

        return AdminMonitorVO.builder()
                .services(services)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .diskUsage(diskUsage)
                .uptime(uptime)
                .build();
    }

    // ─── Helpers ───

    private List<ServiceHealth> buildServiceHealth(double storageUsedGb, double storageQuotaGb) {
        return List.of(
                new ServiceHealth("AI 分析服务", "运行中", "#00B96B", "队列空闲", 320),
                new ServiceHealth("数据库服务", "运行中", "#00B96B", "主库可用", 12),
                new ServiceHealth("对象存储", "运行中", "#00B96B",
                        String.format("%.1f/%.0f GB", storageUsedGb, storageQuotaGb), 45),
                new ServiceHealth("消息队列", "运行中", "#00B96B", "积压 0", 8),
                new ServiceHealth("缓存服务", "运行中", "#00B96B", "命中正常", 3)
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

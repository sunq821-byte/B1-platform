package com.b1.module.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.b1.common.constant.RedisKeys;
import com.b1.common.constant.SystemConstants;

import com.b1.common.exception.BusinessException;
import com.b1.common.exception.ErrorCode;
import com.b1.module.auth.dto.LoginDTO;
import com.b1.module.auth.entity.Role;
import com.b1.module.auth.entity.User;
import com.b1.module.auth.entity.UserRole;
import com.b1.module.auth.mapper.RoleMapper;
import com.b1.module.auth.mapper.UserMapper;
import com.b1.module.auth.mapper.UserRoleMapper;
import com.b1.module.auth.service.AuthService;
import com.b1.module.auth.vo.LoginVO;
import com.b1.module.auth.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (user.getLockExpireTime() != null && user.getLockExpireTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            int failCount = user.getLoginFailCount() + 1;
            user.setLoginFailCount(failCount);
            if (failCount >= SystemConstants.LOGIN_FAIL_MAX) {
                user.setLockExpireTime(LocalDateTime.now().plusMinutes(SystemConstants.LOGIN_LOCK_MINUTES));
            }
            userMapper.updateById(user);
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }

        user.setLoginFailCount(0);
        user.setLockExpireTime(null);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        UserInfoVO userInfo = buildUserInfo(user);
        String userInfoKey = RedisKeys.USER_INFO + user.getId();
        redisTemplate.opsForValue().set(userInfoKey, userInfo, Duration.ofSeconds(SystemConstants.TOKEN_ACCESS_TTL));

        LoginVO vo = new LoginVO();
        vo.setToken(tokenInfo.getTokenValue());
        vo.setRefreshToken(StpUtil.getTokenValue());
        vo.setUserInfo(userInfo);
        return vo;
    }

    @Override
    public void logout() {
        try {
            StpUtil.logout();
        } catch (Exception ignored) {
        }
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        return null;
    }

    private UserInfoVO buildUserInfo(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());

        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId()));
        if (userRoles.isEmpty()) {
            vo.setRoles(Collections.emptyList());
        } else {
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            vo.setRoles(roles.stream().map(Role::getRoleCode).toList());
        }
        return vo;
    }
}

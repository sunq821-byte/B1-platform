package com.b1.module.user.service;

import com.b1.module.user.dto.ChangePasswordDTO;
import com.b1.module.user.dto.UpdateProfileDTO;
import com.b1.module.user.vo.UserProfileVO;

public interface UserService {

    UserProfileVO getProfile();

    UserProfileVO updateProfile(UpdateProfileDTO dto);

    void changePassword(ChangePasswordDTO dto);

    void updateAvatar(String avatarUrl);
}

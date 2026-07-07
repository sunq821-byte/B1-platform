package com.b1.module.teacher.service;

import com.b1.common.result.PageResult;
import com.b1.module.teacher.dto.StandardCreateDTO;
import com.b1.module.teacher.dto.StandardUpdateDTO;
import com.b1.module.teacher.vo.StandardDetailVO;
import com.b1.module.teacher.vo.StandardListVO;
import com.b1.module.teacher.vo.TeacherDashboardVO;

public interface TeacherService {

    TeacherDashboardVO getDashboard();

    PageResult<StandardListVO> listStandards(int page, int pageSize, String keyword);

    StandardDetailVO getStandardDetail(Long standardId);

    StandardListVO createStandard(StandardCreateDTO dto);

    StandardListVO updateStandard(Long standardId, StandardUpdateDTO dto);
}

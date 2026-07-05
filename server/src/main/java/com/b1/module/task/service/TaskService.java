package com.b1.module.task.service;

import com.b1.common.result.PageResult;
import com.b1.module.task.vo.TaskDetailVO;
import com.b1.module.task.vo.TaskVO;

public interface TaskService {

    PageResult<TaskVO> listTasks(int page, int pageSize, String status, String keyword);

    TaskDetailVO getTaskDetail(Long taskId);
}
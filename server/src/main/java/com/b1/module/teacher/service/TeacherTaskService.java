package com.b1.module.teacher.service;

import com.b1.common.result.PageResult;
import com.b1.module.teacher.dto.TaskCreateDTO;
import com.b1.module.teacher.dto.TaskUpdateDTO;
import com.b1.module.teacher.vo.TaskDetailVO;
import com.b1.module.teacher.vo.TaskListVO;

public interface TeacherTaskService {

    PageResult<TaskListVO> listTasks(Long courseId, int page, int pageSize, String keyword, String status);

    PageResult<TaskListVO> listAllTasks(Long courseId, int page, int pageSize, String keyword, String status);

    TaskDetailVO getTaskDetail(Long taskId);

    TaskListVO createTask(Long courseId, TaskCreateDTO dto);

    TaskListVO updateTask(Long taskId, TaskUpdateDTO dto);

    void deleteTask(Long taskId);

    void publishTask(Long taskId);
}

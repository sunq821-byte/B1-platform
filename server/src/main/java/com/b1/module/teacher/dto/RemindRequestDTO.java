package com.b1.module.teacher.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RemindRequestDTO {

    @NotEmpty(message = "请至少选择一个实训任务")
    private List<Long> taskIds;
}

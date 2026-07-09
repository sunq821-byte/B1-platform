package com.b1.module.admin.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogVO {

    private String id;
    private String type;
    private String message;
    private String detail;
    private String operator;
    private String createdAt;
}

package com.b1.module.student.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long notificationId;

    private String title;

    private String content;

    private String type;

    private Long relatedTaskId;

    private Long relatedSubmissionId;

    private Integer isRead;

    private LocalDateTime sentAt;
}

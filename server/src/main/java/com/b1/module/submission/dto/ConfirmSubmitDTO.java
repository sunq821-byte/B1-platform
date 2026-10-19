package com.b1.module.submission.dto;

import lombok.Data;

@Data
public class ConfirmSubmitDTO {

    /** 指定确认的试交记录 ID；为空时使用当前最新试交 */
    private Long trialId;
}

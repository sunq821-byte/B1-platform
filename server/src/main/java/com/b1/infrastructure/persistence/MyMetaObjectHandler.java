package com.b1.infrastructure.persistence;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        try {
            Long loginId = StpUtil.getLoginIdAsLong();
            this.strictInsertFill(metaObject, "createBy", Long.class, loginId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, loginId);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        try {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, StpUtil.getLoginIdAsLong());
        } catch (Exception ignored) {
        }
    }
}

package com.b1.common.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends Result<PageData<T>> {

    private PageResult() {
    }

    public static <T> PageResult<T> of(List<T> list, long page, long pageSize, long total) {
        PageResult<T> r = new PageResult<>();
        r.setCode(0);
        r.setMessage("success");
        r.setSuccess(true);
        r.setTimestamp(System.currentTimeMillis());
        r.setData(PageData.of(list, page, pageSize, total));
        return r;
    }
}

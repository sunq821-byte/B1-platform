package com.b1.common.result;

import lombok.Data;

import java.util.List;

@Data
public class PageData<T> {

    private List<T> list;
    private long page;
    private long pageSize;
    private long total;
    private long totalPages;

    private PageData() {
    }

    public static <T> PageData<T> of(List<T> list, long page, long pageSize, long total) {
        PageData<T> r = new PageData<>();
        r.list = list;
        r.page = page;
        r.pageSize = pageSize;
        r.total = total;
        r.totalPages = (total + pageSize - 1) / pageSize;
        return r;
    }
}

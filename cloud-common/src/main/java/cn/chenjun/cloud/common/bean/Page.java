package cn.chenjun.cloud.common.bean;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Page<T> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 当前第几页
     */
    private int page;
    /**
     * 一页显示多少条记录
     */
    private int pageSize;
    /**
     * 总记录数
     */
    private int total;
    /**
     * 总页数
     */
    private int pageCount;
    /**
     * 当前页的记录
     */
    private List<T> list;
    /**
     * 起始位置
     */
    private int index;
    /**
     * 长度
     */
    private int length;

    public static <T> Page<T> create(int total, int index, int length) {
        int nPageCount;
        if (total % length == 0) {
            nPageCount = total / length;
        } else {
            nPageCount = total / length + 1;
        }
        int page = Math.max((index / length) + 1, 0);
        return Page.<T>builder().page(page).index(index).length(length).pageCount(nPageCount).pageSize(length).total(total).build();
    }

    public static <T> Page<T> create(Page<?> page) {
        Page<T> paginationClass = new Page<T>();
        paginationClass.page = page.page;
        paginationClass.index = page.index;
        paginationClass.length = page.length;
        paginationClass.pageCount = page.pageCount;
        paginationClass.pageSize = page.pageSize;
        paginationClass.total = page.total;
        return paginationClass;
    }
}

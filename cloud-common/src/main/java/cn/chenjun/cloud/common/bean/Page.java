package cn.chenjun.cloud.common.bean;

import cn.chenjun.cloud.common.util.BeanConverter;
import cn.hutool.core.util.ObjectUtil;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static <T> Page<T> create(Page<?> page, List<T> list) {
        Page<T> paginationClass = new Page<T>();
        paginationClass.page = page.page;
        paginationClass.index = page.index;
        paginationClass.length = page.length;
        paginationClass.pageCount = page.pageCount;
        paginationClass.pageSize = page.pageSize;
        paginationClass.total = page.total;
        paginationClass.list = list;
        return paginationClass;
    }

    public static <S, T> Page<T> convert(Page<S> page, BeanConverter.Converter<S, T> converter) {
        Page<T> paginationClass = new Page<T>();
        paginationClass.page = page.page;
        paginationClass.index = page.index;
        paginationClass.length = page.length;
        paginationClass.pageCount = page.pageCount;
        paginationClass.pageSize = page.pageSize;
        paginationClass.total = page.total;
        List<S> sourceList = page.getList();
        if (!ObjectUtil.isEmpty(sourceList)) {
            paginationClass.list = sourceList.stream().map(converter::convert).collect(Collectors.toList());
        } else {
            paginationClass.list = new ArrayList<>();
        }
        return paginationClass;
    }
}

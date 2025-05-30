package cn.chenjun.cloud.common.util;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeanConverter {
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <S, T> T convert(S source, Class<T> targetClass) {
        if (source == null) return null;
        T target = targetClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <S, T> List<T> convert(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) return new ArrayList<>();
        return sourceList.stream().map(source -> convert(source, targetClass)).collect(Collectors.toList());
    }
}

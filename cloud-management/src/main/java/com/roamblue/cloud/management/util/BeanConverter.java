package com.roamblue.cloud.management.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeanConverter {

    public static <T, S> T convert(S source, Converter<S, T> converter) {
        T target = null;
        if (source != null) {
            target = converter.convert(source);
        }
        return target;
    }

    public static <T, S> T convert(S source, Class<T> classz) {
        T target = null;
        if (source != null) {
            Converter<S, T> convert = new SimpleConverter<S, T>(classz);
            target = convert.convert(source);
        }
        return target;
    }

    public static <T, S> List<T> convert(List<S> sourceList, Converter<S, T> converter) {
        List<T> list = null;
        if (sourceList != null && !sourceList.isEmpty()) {
            list = sourceList.stream().map(converter::convert).collect(Collectors.toList());
        } else {
            list = new ArrayList<>(0);
        }
        return list;
    }

    @FunctionalInterface
    public interface Converter<S, T> {
        T convert(S s);
    }

    @Slf4j
    public static final class SimpleConverter<S, T> implements Converter<S, T> {
        final Class<T> classType;

        public SimpleConverter(Class<T> classz) {
            this.classType = classz;
        }

        @Override
        public T convert(S source) {
            try {
                T target = classType.newInstance();
                BeanUtils.copyProperties(source, target);
                return target;
            } catch (Exception e) {
                log.error("bean convert fail.source:{},target:{}", source.getClass(), classType, e);
                return null;
            }
        }
    }

}

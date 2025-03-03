package cn.chenjun.cloud.management.servcie.convert.impl;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.management.servcie.convert.ConfigConvert;

import java.lang.reflect.Type;

public class BaseJsonConvert<T> implements ConfigConvert<T> {

    private final Type type;

    protected BaseJsonConvert(Type type) {
        this.type = type;
    }

    @Override
    public T convert(String value) {
        return GsonBuilderUtil.create().fromJson(value, this.type);
    }
}

package cn.chenjun.cloud.management.servcie.convert.impl;

import cn.chenjun.cloud.management.servcie.convert.ConfigConvert;
import cn.hutool.core.util.NumberUtil;

public class FloatConvert implements ConfigConvert<Float> {
    public static final FloatConvert Default = new FloatConvert();

    @Override
    public Float convert(String value) {
        return NumberUtil.parseFloat(value);
    }
}

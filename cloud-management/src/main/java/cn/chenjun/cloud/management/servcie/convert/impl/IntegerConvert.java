package cn.chenjun.cloud.management.servcie.convert.impl;

import cn.chenjun.cloud.management.servcie.convert.ConfigConvert;
import cn.hutool.core.util.NumberUtil;

public class IntegerConvert implements ConfigConvert<Integer> {
    public static final IntegerConvert Default = new IntegerConvert();

    @Override
    public Integer convert(String value) {
        return NumberUtil.parseInt(value);
    }
}

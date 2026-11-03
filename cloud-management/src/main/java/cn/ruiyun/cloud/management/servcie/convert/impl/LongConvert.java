package cn.ruiyun.cloud.management.servcie.convert.impl;

import cn.ruiyun.cloud.management.servcie.convert.ConfigConvert;
import cn.hutool.core.util.NumberUtil;

public class LongConvert implements ConfigConvert<Long> {
    public static final LongConvert Default = new LongConvert();

    @Override
    public Long convert(String value) {
        return NumberUtil.parseLong(value);
    }
}

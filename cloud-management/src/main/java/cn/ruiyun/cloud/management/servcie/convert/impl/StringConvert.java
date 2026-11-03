package cn.ruiyun.cloud.management.servcie.convert.impl;

import cn.ruiyun.cloud.management.servcie.convert.ConfigConvert;

public class StringConvert implements ConfigConvert<String> {
    public static final StringConvert Default = new StringConvert();

    @Override
    public String convert(String value) {
        return value;
    }
}

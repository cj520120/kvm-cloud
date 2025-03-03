package cn.chenjun.cloud.management.servcie.convert.impl;

import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonListConvert extends BaseJsonConvert<List<Object>> {
    public static final JsonListConvert Default = new JsonListConvert();

    protected JsonListConvert() {
        super(new TypeToken<List<Object>>() {
        }.getType());
    }
}

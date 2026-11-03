package cn.ruiyun.cloud.management.servcie.convert;

public interface ConfigConvert<T> {
    T convert(String value);
}

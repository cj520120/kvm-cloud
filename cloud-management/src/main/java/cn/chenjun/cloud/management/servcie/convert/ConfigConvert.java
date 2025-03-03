package cn.chenjun.cloud.management.servcie.convert;

public interface ConfigConvert<T> {
    T convert(String value);
}

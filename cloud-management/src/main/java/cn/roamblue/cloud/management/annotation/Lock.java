package cn.roamblue.cloud.management.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {
    /**
     * key
     * @return
     */
    String value();

    /**
     * 是否为写锁
     * @return
     */
    boolean write()default true;
    /**
     * 超时时间
     * @return
     */
    int timeout() default 3;

    /**
     * 超时单位
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}

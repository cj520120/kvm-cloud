package cn.chenjun.cloud.management.annotation;

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
     *
     * @return
     */
    String value();

    /**
     * 是否为写锁
     *
     * @return
     */
    boolean write() default true;

}

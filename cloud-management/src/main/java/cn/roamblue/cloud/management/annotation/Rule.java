package cn.roamblue.cloud.management.annotation;

import cn.roamblue.cloud.management.util.RuleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenjun
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
    String[] permissions() default {};
}
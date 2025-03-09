package cn.chenjun.cloud.agent.operate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenjun
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DispatchBind {
    /**
     * 绑定命令
     *
     * @return
     */
    String command();

    /**
     * 是否异步执行
     *
     * @return
     */
    boolean async() default true;
}

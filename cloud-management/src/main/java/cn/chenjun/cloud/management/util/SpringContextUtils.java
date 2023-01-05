package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.ErrorCode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author chenjun
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {


    private static ApplicationContext applicationContext = null;

    public static <T> T getBean(Class<T> requiredType) {
        if (applicationContext != null) {
            return applicationContext.getBean(requiredType);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "系统未初始化完成....");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtils.applicationContext = applicationContext;
    }

}
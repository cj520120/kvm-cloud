package com.roamblue.cloud.management.util;

import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 提供静态方法获取spring bean实例
 *
 * @author
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {


    private static ApplicationContext _applicationContext = null;

    public static <T> T getBean(Class<T> requiredType) {
        if (_applicationContext != null) {
            return _applicationContext.getBean(requiredType);
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "系统未初始化完成....");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

}
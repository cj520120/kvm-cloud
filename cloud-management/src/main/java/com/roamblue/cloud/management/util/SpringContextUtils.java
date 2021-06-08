package com.roamblue.cloud.management.util;

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

    private static SpringContextUtils _self;

    public static <T> T getBean(Class<T> requiredType) {
        if (_applicationContext != null) {
            return _applicationContext.getBean(requiredType);
        } else {
            throw new org.springframework.context.ApplicationContextException("ApplicationContext has not been set.");
        }
    }


    private static SpringContextUtils self() {
        if (_self == null) {
            if (_applicationContext != null) {
                _self = _applicationContext.getBean(SpringContextUtils.class);
            } else {
                throw new org.springframework.context.ApplicationContextException(
                        "ApplicationContext has not been set.");
            }
        }
        return _self;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

}
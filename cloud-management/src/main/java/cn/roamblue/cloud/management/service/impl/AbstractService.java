package cn.roamblue.cloud.management.service.impl;

import cn.roamblue.cloud.management.util.LocaleMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName: AbstractService
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/3 下午3:23
 */
public abstract class AbstractService {
    @Autowired
    protected LocaleMessage localeMessage;
}

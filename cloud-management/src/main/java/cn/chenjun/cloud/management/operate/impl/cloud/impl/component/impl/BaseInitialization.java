package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl;

import cn.chenjun.cloud.management.data.dao.GuestNetworkDao;
import cn.chenjun.cloud.management.data.dao.NetworkDao;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.ComponentInitialization;
import cn.chenjun.cloud.management.servcie.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class BaseInitialization implements ComponentInitialization {


    @Autowired
    protected NetworkDao networkDao;
    @Autowired
    protected GuestNetworkDao guestNetworkDao;
    @Autowired
    protected ConfigService configService;

    @Override
    public int getOrder() {
        return 100;
    }
}

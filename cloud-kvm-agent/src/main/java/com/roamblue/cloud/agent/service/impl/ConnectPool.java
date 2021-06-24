package com.roamblue.cloud.agent.service.impl;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.libvirt.Connect;
import org.springframework.stereotype.Service;

/**
 * @author chenjun
 */
@Service
public class ConnectPool extends GenericObjectPool<Connect> {
    public ConnectPool(GenericObjectPoolConfig<Connect> config) {
        super(new ConnectFactory(), config);
    }
}

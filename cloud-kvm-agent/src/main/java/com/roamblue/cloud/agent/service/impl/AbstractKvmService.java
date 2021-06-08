package com.roamblue.cloud.agent.service.impl;

import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractKvmService {

    @Autowired
    private ConnectPool connectPool;

    protected <V> V excute(Runner<V> runner) {
        Connect connect = null;
        try {
            connect = connectPool.borrowObject();
            return runner.call(connect);
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            log.error("执行出错", err);
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        } finally {
            if (connect != null) {
                connectPool.returnObject(connect);
            }
        }
    }

    @FunctionalInterface
    public interface Runner<V> {
        V call(Connect connect) throws Exception;
    }
}

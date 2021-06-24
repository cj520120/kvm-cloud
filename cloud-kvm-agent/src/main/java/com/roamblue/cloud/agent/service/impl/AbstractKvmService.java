package com.roamblue.cloud.agent.service.impl;

import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Error;
import org.libvirt.LibvirtException;
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
        } catch (LibvirtException err) {
            if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                throw new CodeException(ErrorCode.VM_NOT_FOUND, "VM未启动");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_NETWORK)) {
                throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "VM网络未找到");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_CONNECT)) {
                throw new CodeException(ErrorCode.QEMU_NOT_CONNECT, "QemuAgent 未安装或VM未启动完成");
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            }
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

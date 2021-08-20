package cn.roamblue.cloud.agent.service.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.Error;
import org.libvirt.LibvirtException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractKvmService {

    @Autowired
    private ConnectPool connectPool;

    /**
     * kvm执行
     *
     * @param runner
     * @param <V>
     * @return
     */
    protected <V> V execute(Runner<V> runner) {
        Connect connect = null;
        try {
            connect = connectPool.borrowObject();
            return runner.call(connect);
        } catch (LibvirtException err) {
            if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "domain not found or unexpectedly disappeared");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_NETWORK)) {
                throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "network not found");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_CONNECT)) {
                throw new CodeException(ErrorCode.QEMU_NOT_CONNECT, "can't connect to hypervisor");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_POOL)) {
                throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "storage not found");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_VOL)) {
                throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "storage vol not found");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_INVALID_STORAGE_VOL)) {
                throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "invalid storage vol object");
            } else {
                log.error("call error", err.getMessage());
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            }
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            log.error("execute fail.", err);
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        } finally {
            if (connect != null) {
                connectPool.returnObject(connect);
            }
        }
    }

    @FunctionalInterface
    public interface Runner<V> {
        /**
         * 执行
         *
         * @param connect
         * @return
         * @throws Exception
         */
        V call(Connect connect) throws Exception;
    }
}

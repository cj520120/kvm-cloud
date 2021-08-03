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
    protected <V> V excute(Runner<V> runner) {
        Connect connect = null;
        try {
            connect = connectPool.borrowObject();
            return runner.call(connect);
        } catch (LibvirtException err) {
            if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "agent vm not found");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_NETWORK)) {
                throw new CodeException(ErrorCode.NETWORK_NOT_FOUND, "agent vm network not found");
            } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_CONNECT)) {
                throw new CodeException(ErrorCode.QEMU_NOT_CONNECT, "agent QemuAgent not connect");
            } else {
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

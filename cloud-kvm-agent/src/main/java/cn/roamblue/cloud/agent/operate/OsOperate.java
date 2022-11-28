package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.agent.OsRequest;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
public interface OsOperate {
    /**
     * 启动
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void start(Connect connect, OsRequest.Start request) throws Exception;

    /**
     * 关机
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void shutdown(Connect connect, OsRequest.Shutdown request) throws Exception;

    /**
     * 重启
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void reboot(Connect connect, OsRequest.Reboot request) throws Exception;

    /**
     * 执行qma
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void qma(Connect connect, OsRequest.Qma request) throws Exception;

    /**
     * 销毁
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroy(Connect connect, OsRequest.Destroy request) throws Exception;
}

package cn.roamblue.cloud.agent.operate;

import cn.roamblue.cloud.common.bean.GuestInfo;
import cn.roamblue.cloud.common.bean.*;
import org.libvirt.Connect;

import java.util.List;

/**
 * @author chenjun
 */
public interface OsOperate {

    /**
     * 获取客户机信息
     *
     * @param connect
     * @param request
     * @return
     * @throws Exception
     */
    GuestInfo getGustInfo(Connect connect, GuestInfoRequest request) throws Exception;

    /**
     * 获取所有客户机
     * @param connect
     * @return
     * @throws Exception
     */
    List<GuestInfo> listAllGuestInfo(Connect connect) throws Exception;

    /**
     * 批量获取客户机信息
     *
     * @param connect
     * @param batchRequest
     * @return
     * @throws Exception
     */
    List<GuestInfo> batchGustInfo(Connect connect, List<GuestInfoRequest> batchRequest) throws Exception;

    /**
     * 启动
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    GuestInfo start(Connect connect, GuestStartRequest request) throws Exception;


    /**
     * 关机
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void shutdown(Connect connect, GuestShutdownRequest request) throws Exception;

    /**
     * 重启
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void reboot(Connect connect, GuestRebootRequest request) throws Exception;

    /**
     * 挂载光驱
     * @param connect
     * @param request
     * @throws Exception
     */
    void attachCdRoom(Connect connect, OsCdRoom request) throws Exception;

    /**
     * 卸载光驱
     * @param connect
     * @param request
     * @throws Exception
     */
    void detachCdRoom(Connect connect, OsCdRoom request) throws Exception;

    /**
     * 挂载磁盘
     * @param connect
     * @param request
     * @throws Exception
     */
    void attachDisk(Connect connect, OsDisk request) throws Exception;

    /**
     * 卸载磁盘
     * @param connect
     * @param request
     * @throws Exception
     */
    void detachDisk(Connect connect, OsDisk request) throws Exception;

    /**
     * 挂载网卡
     * @param connect
     * @param request
     * @throws Exception
     */
    void attachNic(Connect connect, OsNic request) throws Exception;

    /**
     * 卸载网卡
     * @param connect
     * @param request
     * @throws Exception
     */
    void detachNic(Connect connect, OsNic request) throws Exception;


    /**
     * 执行qma
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void qma(Connect connect, GuestQmaRequest request) throws Exception;

    /**
     * 销毁
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    void destroy(Connect connect, GuestDestroyRequest request) throws Exception;
}

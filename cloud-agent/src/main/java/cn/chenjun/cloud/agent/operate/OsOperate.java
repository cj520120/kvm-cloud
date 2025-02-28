package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.common.bean.*;
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
     *
     * @param connect
     * @return
     * @throws Exception
     */
    List<GuestInfo> listAllGuestInfo(Connect connect, NoneRequest request) throws Exception;

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
     * @return
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
    Void shutdown(Connect connect, GuestShutdownRequest request) throws Exception;

    /**
     * 重启
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void reboot(Connect connect, GuestRebootRequest request) throws Exception;

    /**
     * 挂载光驱
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void attachCdRoom(Connect connect, ChangeGuestCdRoomRequest request) throws Exception;

    /**
     * 卸载光驱
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void detachCdRoom(Connect connect, ChangeGuestCdRoomRequest request) throws Exception;

    /**
     * 挂载磁盘
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void attachDisk(Connect connect, ChangeGuestDiskRequest request) throws Exception;

    /**
     * 卸载磁盘
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void detachDisk(Connect connect, ChangeGuestDiskRequest request) throws Exception;

    /**
     * 挂载网卡
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void attachNic(Connect connect, ChangeGuestInterfaceRequest request) throws Exception;

    /**
     * 卸载网卡
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void detachNic(Connect connect, ChangeGuestInterfaceRequest request) throws Exception;


    /**
     * 执行qma
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void qma(Connect connect, GuestQmaRequest request) throws Exception;

    /**
     * 销毁
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void destroy(Connect connect, GuestDestroyRequest request) throws Exception;

    /**
     * 虚拟机迁移
     *
     * @param connect
     * @param request
     * @throws Exception
     */
    Void migrate(Connect connect, GuestMigrateRequest request) throws Exception;
}

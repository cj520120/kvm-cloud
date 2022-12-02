package cn.roamblue.cloud.agent.service;

import cn.roamblue.cloud.common.bean.GuestInfo;
import cn.roamblue.cloud.common.agent.VmModel;
import cn.roamblue.cloud.common.agent.VmSnapshotModel;
import cn.roamblue.cloud.common.agent.VmStaticsModel;

import java.util.List;

/**
 * @author chenjun
 */
public interface KvmVmService {
    /**
     * 获取VM列表
     *
     * @return
     */
    List<GuestInfo> listVm();

    /**
     * 获取VM监控指标
     *
     * @return
     */
    List<VmStaticsModel> listVmStatics();

    /**
     * 根据名称获取VM
     *
     * @param name
     * @return
     */
    GuestInfo findByName(String name);

    /**
     * 获取虚拟机快照列表
     *
     * @param name 虚拟机名称
     * @return
     */
    List<VmSnapshotModel> listSnapshot(String name);

    /**
     * 创建虚拟机快照
     *
     * @param name        虚拟机名称
     * @return
     */
    VmSnapshotModel createSnapshot(String name);

    /**
     * 恢复虚拟机快照
     *
     * @param name         虚拟机名称
     * @param snapshotName 快照名称
     */
    void revertToSnapshot(String name, String snapshotName);

    /**
     * 删除虚拟机快照
     *
     * @param name
     * @param snapshotName
     */
    void deleteSnapshot(String name, String snapshotName);

    /**
     * 重启VM
     *
     * @param name
     */
    void restart(String name);

    /**
     * 销毁VM
     *
     * @param name
     */
    void destroy(String name);

    /**
     * 停止VM
     *
     * @param name
     * @param timeout 超时时间
     */
    void stop(String name, int timeout);

    /**
     * 附加设备
     *
     * @param name
     * @param xml
     */
    void attachDevice(String name, String xml);

    /**
     * 取消附加设备
     *
     * @param name
     * @param xml
     */
    void detachDevice(String name, String xml);

    /**
     * 启动
     *
     * @param info
     * @return
     */
    GuestInfo start(VmModel info);

    /**
     * 更新设备
     *
     * @param name
     * @param xml
     */
    void updateDevice(String name, String xml);
}

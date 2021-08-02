package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.TemplateInfo;
import cn.roamblue.cloud.management.bean.VmInfo;
import cn.roamblue.cloud.management.bean.VmNetworkInfo;
import cn.roamblue.cloud.management.bean.VolumeInfo;

/**
 * @author chenjun
 */
public interface VmService {
    /**
     * Vm类型
     *
     * @return
     */
    String getType();

    /**
     * 创建虚拟机
     *
     * @param description
     * @param calculationSchemeId
     * @param clusterId
     * @param storageId
     * @param templateId
     * @param diskSize
     * @param network
     * @param groupId
     * @return
     */
    VmInfo create(String description, int calculationSchemeId, int clusterId, int storageId, int templateId, long diskSize, int network, int groupId);

    /**
     * 附加网卡
     * @param vmId 虚拟机ID
     * @param networkId  网络ID
     * @return
     */
    VmNetworkInfo attachNetwork(int vmId, int networkId);

    /**
     * 卸载网卡
     * @param vmId
     * @param vmNetworkId
     */
    void detachNetwork(int vmId,int vmNetworkId);

    /**
     * 启动虚拟机
     *
     * @param id
     * @param hostId
     * @return
     */
    VmInfo start(int id, int hostId);

    /**
     * 停止虚拟机
     *
     * @param id
     * @param force
     * @return
     */
    VmInfo stop(int id, boolean force);

    /**
     * 重启虚拟机
     *
     * @param id
     * @param force
     * @return
     */
    VmInfo reboot(int id, boolean force);

    /**
     * 销毁虚拟机
     *
     * @param id
     * @return
     */
    void destroy(int id);


    /**
     * 恢复虚拟机
     *
     * @param vmId
     * @return
     */
    VmInfo resume(int vmId);

    /**
     * 挂载/卸载ISO
     *
     * @param id
     * @param isoTemplateId
     * @return
     */
    VmInfo changeCdRoom(int id, int isoTemplateId);

    /**
     * 挂载磁盘
     *
     * @param vmId
     * @param volumeId
     * @return
     */
    VolumeInfo attachDisk(int vmId, int volumeId);

    /**
     * 卸载磁盘
     *
     * @param vmId
     * @param volumeId
     * @return
     */
    VolumeInfo detachDisk(int vmId, int volumeId);

    /**
     * 修改实例信息
     *
     * @param vmId
     * @param description
     * @param calculationSchemeId
     * @param groupId
     * @return
     */
    VmInfo modify(int vmId, String description, int calculationSchemeId, int groupId);

    /**
     * 创建模版
     *
     * @param vmId
     * @param name
     * @return
     */
    TemplateInfo createTemplate(int vmId, String name);

    /**
     * 系统重装
     *
     * @param vmId
     * @param templateId
     * @return
     */
    VmInfo reInstall(int vmId, int templateId);

}

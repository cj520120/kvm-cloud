package com.roamblue.cloud.management.service;

import com.roamblue.cloud.management.bean.TemplateInfo;
import com.roamblue.cloud.management.bean.VmInfo;
import com.roamblue.cloud.management.bean.VolumeInfo;

public interface VmService {
    /**
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
     * @param hostId
     * @param templateId
     * @param diskSize
     * @param network
     * @return
     */
    VmInfo create(String description, int calculationSchemeId, int clusterId, int storageId, int hostId, int templateId, long diskSize, int network, int groupId);

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
     * @return
     */
    VmInfo modify(int vmId, String description, int calculationSchemeId, int groupId);

    /**
     * 创建模版
     *
     * @param vmId
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

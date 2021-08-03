package cn.roamblue.cloud.management.ui;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.bean.*;

import java.util.List;

/**
 * @author chenjun
 */
public interface VmUiService {
    /**
     * VM列表
     *
     * @return
     */
    ResultUtil<List<VmInfo>> listAllVm();

    /**
     * 搜索VM
     *
     * @param clusterId
     * @param hostId
     * @param groupId
     * @param type
     * @param status
     * @return
     */
    ResultUtil<List<VmInfo>> search(int clusterId, int hostId, int groupId, String type, String status);

    /***
     * 根据ID获取VM信息
     * @param vmId
     * @return
     */
    ResultUtil<VmInfo> findVmById(int vmId);

    /**
     * 获取VM监控信息
     *
     * @param vmId
     * @return
     */
    ResultUtil<List<VmStatisticsInfo>> listVmStatistics(int vmId);

    /**
     * 修改VM信息
     *
     * @param vmId
     * @param description
     * @param calculationSchemeId
     * @param groupId
     * @return
     */
    ResultUtil<VmInfo> modify(int vmId, String description, int calculationSchemeId, int groupId);

    /**
     * 获取VNC地址信息
     *
     * @param id
     * @return
     */
    ResultUtil<VncInfo> findVncByVmId(int id);

    /**
     * 创建VM
     *
     * @param name
     * @param clusterId
     * @param storageId
     * @param calculationSchemeId
     * @param templateId
     * @param size
     * @param networkId
     * @param groupId
     * @return
     */
    ResultUtil<VmInfo> create(String name, int clusterId, int storageId, int calculationSchemeId, int templateId, long size, int networkId, int groupId);

    /**
     * 启动VM
     *
     * @param id
     * @param hostId
     * @return
     */
    ResultUtil<VmInfo> start(int id, int hostId);

    /**
     * 批量重启
     * @param ids
     * @param hostId
     * @return
     */
    ResultUtil<List<ResultUtil<VmInfo>>> batchStart(List<Integer> ids, int hostId);
    /**
     * 停止VM
     *
     * @param id
     * @param force
     * @return
     */
    ResultUtil<VmInfo> stop(int id, boolean force);
    /**
     * 批量停止VM
     *
     * @param ids
     * @param force
     * @return
     */
    ResultUtil<List<ResultUtil<VmInfo>>> batchStop(List<Integer> ids, boolean force);
    /**
     * 重启VM
     *
     * @param id
     * @param force
     * @return
     */
    ResultUtil<VmInfo> reboot(int id, boolean force);

    /**
     * 批量重启VM
     * @param ids
     * @param force
     * @return
     */
    ResultUtil<List<ResultUtil<VmInfo>>> batchReboot(List<Integer> ids, boolean force);
    /**
     * 重新安装VM
     *
     * @param vmId
     * @param templateId
     * @return
     */
    ResultUtil<VmInfo> reInstall(int vmId, int templateId);

    /**
     * 创建VM模版
     *
     * @param id
     * @param name
     * @return
     */
    ResultUtil<TemplateInfo> createTemplate(int id, String name);

    /**
     * 销毁VM
     *
     * @param id
     * @return
     */
    ResultUtil<VmInfo> destroyVmById(int id);

    /**
     * 恢复VM
     *
     * @param id
     * @return
     */
    ResultUtil<VmInfo> resume(int id);

    /**
     * 附加光盘
     *
     * @param id
     * @param iso
     * @return
     */
    ResultUtil<VmInfo> attachCdRoom(int id, int iso);

    /**
     * 取消附加光盘
     *
     * @param id
     * @return
     */
    ResultUtil<VmInfo> detachCdRoom(int id);

    /**
     * 附加磁盘
     *
     * @param id
     * @param volume
     * @return
     */
    ResultUtil<VolumeInfo> attachDisk(int id, int volume);

    /**
     * 取消附加磁盘
     *
     * @param id
     * @param volume
     * @return
     */
    ResultUtil<VolumeInfo> detachDisk(int id, int volume);



    /**
     * 附加网卡
     *
     * @param vmId
     * @param networkId
     * @return
     */
    ResultUtil<VmNetworkInfo> attachNetwork(int vmId, int networkId);

    /**
     * 取消附加网卡
     *
     * @param vmId
     * @param vmNetworkId
     * @return
     */
    ResultUtil<Void> detachNetwork(int vmId, int vmNetworkId);
}

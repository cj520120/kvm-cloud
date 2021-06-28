package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.VmInfo;
import cn.roamblue.cloud.management.bean.VmStatisticsInfo;
import cn.roamblue.cloud.management.bean.VncInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface InstanceService {

    /**
     * 获取主机管理类
     *
     * @param id
     * @return
     */
    VmService getVmServiceByVmId(int id);

    /**
     * 根据类型获取主机管理类
     *
     * @param type
     * @return
     */
    VmService getVmServiceByType(String type);

    /**
     * 查找实例
     *
     * @param id
     * @return
     */
    VmInfo findVmById(int id);

    /**
     * 获取VNC信息
     *
     * @param vmId
     * @return
     */
    VncInfo findVncById(int vmId);

    /**
     * 搜索
     *
     * @param clusterId
     * @param hostId
     * @param groupId
     * @param type
     * @param status
     * @return
     */
    List<VmInfo> search(int clusterId, int hostId, int groupId, String type, String status);

    /**
     * 获取所有VM
     *
     * @return
     */
    List<VmInfo> listAllVm();

    /**
     * 获取实例监控信息
     *
     * @param vmId
     * @return
     */
    List<VmStatisticsInfo> listVmStatisticsById(int vmId);
}

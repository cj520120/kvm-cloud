package cn.roamblue.cloud.management.controller;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.bean.*;
import cn.roamblue.cloud.management.ui.VmUiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 虚拟管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class VmController {
    @Autowired
    private VmUiService vmUiService;

    /**
     * 获取所有虚拟机
     *
     * @return
     */
    @Login
    @GetMapping("/management/vm")
    public ResultUtil<List<VmInfo>> listAllVm() {
        return vmUiService.listAllVm();
    }

    /**
     * 搜索虚拟机
     *
     * @param clusterId 集群ID
     * @param hostId    主机ID
     * @param groupId   群组
     * @param type      类型
     * @param status    状态
     * @return
     */
    @Login
    @GetMapping("/management/vm/search")
    public ResultUtil<List<VmInfo>> search(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("hostId") int hostId,
            @RequestParam("groupId") int groupId,
            @RequestParam("type") String type,
            @RequestParam("status") String status) {

        return vmUiService.search(clusterId, hostId, groupId, type, status);
    }

    /**
     * 获取虚拟机信息
     *
     * @param vmId 虚拟机ID
     * @return
     */
    @Login
    @GetMapping("/management/vm/info")
    public ResultUtil<VmInfo> findVmById(@RequestParam("vmId") int vmId) {
        return vmUiService.findVmById(vmId);
    }

    /**
     * 获取虚拟机监控信息
     *
     * @param vmId 虚拟机ID
     * @return
     */
    @Login
    @GetMapping("/management/vm/statistics")
    public ResultUtil<List<VmStatisticsInfo>> listVmStatistics(@RequestParam("vmId") int vmId) {
        return vmUiService.listVmStatistics(vmId);
    }

    /**
     * 更新虚拟机信息
     *
     * @param vmId                虚拟机ID
     * @param description         备注
     * @param calculationSchemeId 计算方案
     * @param groupId             群组
     * @return
     */
    @Login
    @PostMapping("/management/vm/modify")
    public ResultUtil<VmInfo> modifyInstance(
            @RequestParam("id") int vmId,
            @RequestParam("description") String description,
            @RequestParam("calculationSchemeId") int calculationSchemeId,
            @RequestParam("groupId") int groupId) {

        return vmUiService.modify(vmId, description, calculationSchemeId, groupId);
    }

    /**
     * 获取虚拟机VNC
     *
     * @param id 虚拟机ID
     * @return
     */
    @Login
    @GetMapping("/management/vm/vnc")
    public ResultUtil<VncInfo> findVncByVmId(@RequestParam("id") int id) {
        return vmUiService.findVncByVmId(id);
    }

    /**
     * 创建虚拟机
     *
     * @param name                虚拟机名称
     * @param clusterId           集群ID
     * @param storageId           存储ID
     * @param hostId              主机ID
     * @param calculationSchemeId 计算方案
     * @param templateId          模版ID
     * @param size                磁盘大小(GB)
     * @param networkId           网络ID
     * @param groupId             群组ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/create")
    public ResultUtil<VmInfo> create(@RequestParam("name") String name,
                                     @RequestParam("clusterId") int clusterId,
                                     @RequestParam("storageId") int storageId,
                                     @RequestParam("hostId") int hostId,
                                     @RequestParam("calculationSchemeId") int calculationSchemeId,
                                     @RequestParam("templateId") int templateId,
                                     @RequestParam("size") long size,
                                     @RequestParam("networkId") int networkId,
                                     @RequestParam("groupId") int groupId) {

        return vmUiService.create(name, clusterId, storageId, hostId, calculationSchemeId, templateId, size, networkId, groupId);
    }

    /**
     * 启动虚拟机
     *
     * @param id     虚拟机ID
     * @param hostId 主机ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/start")
    public ResultUtil<VmInfo> start(@RequestParam("id") int id, @RequestParam("hostId") int hostId) {
        return vmUiService.start(id, hostId);
    }

    /**
     * 停止虚拟机
     *
     * @param id    虚拟机ID
     * @param force 是否强制
     * @return
     */
    @Login
    @PostMapping("/management/vm/stop")
    public ResultUtil<VmInfo> stop(@RequestParam("id") int id, @RequestParam(value = "force", defaultValue = "false") boolean force) {
        return vmUiService.stop(id, force);
    }

    /**
     * 重启虚拟机
     *
     * @param id    虚拟机ID
     * @param force 是否强制
     * @return
     */
    @Login
    @PostMapping("/management/vm/reboot")
    public ResultUtil<VmInfo> reboot(@RequestParam("id") int id, @RequestParam(value = "force", defaultValue = "false") boolean force) {
        return vmUiService.reboot(id, force);
    }

    /**
     * 重装虚拟机
     *
     * @param vmId       虚拟机ID
     * @param templateId 模版ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/reinstall")
    public ResultUtil<VmInfo> reInstall(@RequestParam("id") int vmId, @RequestParam("templateId") int templateId) {
        return vmUiService.reInstall(vmId, templateId);
    }

    /**
     * management
     *
     * @param id   虚拟机ID
     * @param name 模版名称
     * @return
     */
    @Login
    @PostMapping("/management/vm/template")
    public ResultUtil<TemplateInfo> createTemplate(@RequestParam("id") int id, @RequestParam("name") String name) {

        return vmUiService.createTemplate(id, name);
    }

    /**
     * 销毁虚拟机
     *
     * @param id 虚拟机ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/destroy")
    public ResultUtil<VmInfo> destroy(@RequestParam("id") int id) {

        return vmUiService.destroyVmById(id);
    }

    /**
     * 恢复虚拟机
     *
     * @param id 虚拟机ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/resume")
    public ResultUtil<VmInfo> resume(@RequestParam("id") int id) {
        return vmUiService.resume(id);
    }

    /**
     * 挂载光盘
     *
     * @param id  虚拟机ID
     * @param iso 光盘模版ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/attach/cdroom")
    public ResultUtil<VmInfo> attachCdRoom(@RequestParam("id") int id, @RequestParam("iso") int iso) {

        return vmUiService.attachCdRoom(id, iso);
    }

    /**
     * 取消挂载光盘
     *
     * @param id 虚拟机ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/detach/cdroom")
    public ResultUtil<VmInfo> detachCdRoom(@RequestParam("id") int id) {
        return vmUiService.detachCdRoom(id);
    }

    /**
     * 挂载磁盘
     *
     * @param id     虚拟机ID
     * @param volume 磁盘ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/attach/disk")
    public ResultUtil<VolumeInfo> attachDisk(@RequestParam("id") int id, @RequestParam("volume") int volume) {
        return vmUiService.attachDisk(id, volume);
    }

    /**
     * 取消挂载磁盘
     *
     * @param id     虚拟机ID
     * @param volume 磁盘ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/detach/disk")
    public ResultUtil<VolumeInfo> detachDisk(@RequestParam("id") int id, @RequestParam("volume") int volume) {
        return vmUiService.detachDisk(id, volume);
    }



    /**
     * 挂载网络
     *
     * @param vmId     虚拟机ID
     * @param networkId 网络ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/attach/network")
    public ResultUtil<VmNetworkInfo> attachNetwork(@RequestParam("vmId") int vmId, @RequestParam("networkId") int networkId) {
        return vmUiService.attachNetwork(vmId, networkId);
    }

    /**
     * 取消挂载磁盘
     *
     * @param vmId     虚拟机ID
     * @param vmNetworkId 虚拟机网卡ID
     * @return
     */
    @Login
    @PostMapping("/management/vm/detach/network")
    public ResultUtil<Void> detachNetwork(@RequestParam("vmId") int vmId, @RequestParam("vmNetworkId") int vmNetworkId) {
        return vmUiService.detachNetwork(vmId, vmNetworkId);
    }
}

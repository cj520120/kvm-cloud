package com.roamblue.cloud.management.controller;

import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.management.annotation.Login;
import com.roamblue.cloud.management.bean.*;
import com.roamblue.cloud.management.ui.VmUiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "虚拟管理")
@Slf4j
public class InstanceController {
    @Autowired
    private VmUiService vmUiService;

    @Login
    @GetMapping("/management/vm")
    @ApiOperation(value = "获取所有虚拟机")
    public ResultUtil<List<VmInfo>> listAllVm() {
        return vmUiService.listAllVm();
    }

    @Login
    @GetMapping("/management/vm/search")
    @ApiOperation(value = "搜索虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clusterId", value = "集群ID", defaultValue = "0"),
            @ApiImplicitParam(name = "hostId", value = "主机ID", defaultValue = "0"),
            @ApiImplicitParam(name = "type", value = "类型"),
            @ApiImplicitParam(name = "status", value = "状态"),
            @ApiImplicitParam(name = "groupId", value = "群组"),
    })
    public ResultUtil<List<VmInfo>> search(
            @RequestParam("clusterId") int clusterId,
            @RequestParam("hostId") int hostId,
            @RequestParam("groupId") int groupId,
            @RequestParam("type") String type,
            @RequestParam("status") String status) {

        return vmUiService.search(clusterId, hostId, groupId, type, status);
    }

    @Login
    @GetMapping("/management/vm/info")
    @ApiOperation(value = "获取虚拟机信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "vmId", value = "虚拟机ID")
    })
    public ResultUtil<VmInfo> findVmById(@RequestParam("vmId") int vmId) {
        return vmUiService.findVmById(vmId);
    }


    @Login
    @GetMapping("/management/vm/statistics")
    @ApiOperation(value = "获取虚拟机监控信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "vmId", value = "虚拟机ID")
    })
    public ResultUtil<List<VmStatisticsInfo>> listVmStatistics(@RequestParam("vmId") int vmId) {
        return vmUiService.listVmStatistics(vmId);
    }

    @Login
    @PostMapping("/management/vm/modify")
    @ApiOperation(value = "更新虚拟机信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "description", value = "备注"),
            @ApiImplicitParam(name = "calculationSchemeId", value = "计算方案"),
            @ApiImplicitParam(name = "groupId", value = "群组"),
    })
    public ResultUtil<VmInfo> modifyInstance(
            @RequestParam("id") int vmId,
            @RequestParam("description") String description,
            @RequestParam("calculationSchemeId") int calculationSchemeId,
            @RequestParam("groupId") int groupId) {

        return vmUiService.modifyInstance(vmId, description, calculationSchemeId, groupId);
    }

    @Login
    @GetMapping("/management/vm/vnc")
    @ApiOperation(value = "获取虚拟机VNC")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID")
    })
    public ResultUtil<VncInfo> findVncByVmId(@RequestParam("id") int id) {
        return vmUiService.findVncByVmId(id);
    }

    @Login
    @PostMapping("/management/vm/create")
    @ApiOperation(value = "创建虚拟机")
    @ApiImplicitParams({

            @ApiImplicitParam(name = "name", value = "虚拟机名称"),
            @ApiImplicitParam(name = "clusterId", value = "集群ID"),
            @ApiImplicitParam(name = "storageId", value = "存储ID"),
            @ApiImplicitParam(name = "hostId", value = "主机ID"),
            @ApiImplicitParam(name = "calculationSchemeId", value = "计算方案"),
            @ApiImplicitParam(name = "templateId", value = "模版ID"),
            @ApiImplicitParam(name = "size", value = "磁盘大小"),
            @ApiImplicitParam(name = "networkId", value = "网络ID"),
            @ApiImplicitParam(name = "groupId", value = "群组ID"),
    })
    public ResultUtil<VmInfo> create(@RequestParam("name") String name,
                                     @RequestParam("clusterId") int clusterId,
                                     @RequestParam("storageId") int storageId,
                                     @RequestParam("hostId") int hostId,
                                     @RequestParam("calculationSchemeId") int calculationSchemeId,
                                     @RequestParam("templateId") int templateId,
                                     @RequestParam("size") long size,
                                     @RequestParam("networkId") int networkId,
                                     @RequestParam("groupId") int groupId) {

        //
        return vmUiService.create(name, clusterId, storageId, hostId, calculationSchemeId, templateId, size, networkId, groupId);
    }

    @Login
    @PostMapping("/management/vm/start")
    @ApiOperation(value = "启动虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "hostId", value = "主机ID")
    })
    public ResultUtil<VmInfo> start(@RequestParam("id") int id, @RequestParam("hostId") int hostId) {
        return vmUiService.start(id, hostId);
    }

    @Login
    @PostMapping("/management/vm/stop")
    @ApiOperation(value = "停止虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "force", value = "是否强制")
    })
    public ResultUtil<VmInfo> stop(@RequestParam("id") int id, @RequestParam(value = "force", defaultValue = "false") boolean force) {
        return vmUiService.stop(id, force);
    }

    @Login
    @PostMapping("/management/vm/reboot")
    @ApiOperation(value = "重启虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "force", value = "是否强制")
    })
    public ResultUtil<VmInfo> reboot(@RequestParam("id") int id, @RequestParam(value = "force", defaultValue = "false") boolean force) {
        return vmUiService.reboot(id, force);
    }

    @Login
    @PostMapping("/management/vm/reinstall")
    @ApiOperation(value = "重装虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "templateId", value = "模版ID")
    })
    public ResultUtil<VmInfo> reInstall(@RequestParam("id") int vmId, @RequestParam("templateId") int templateId) {
        return vmUiService.reInstall(vmId, templateId);
    }

    @Login
    @PostMapping("/management/vm/template")
    @ApiOperation(value = "创建模版")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "name", value = "模版名称")
    })
    public ResultUtil<TemplateInfo> createTemplate(@RequestParam("id") int id, @RequestParam("name") String name) {

        return vmUiService.createTemplate(id, name);
    }

    @Login
    @PostMapping("/management/vm/destroy")
    @ApiOperation(value = "销毁虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID")
    })
    public ResultUtil<VmInfo> destroy(@RequestParam("id") int id) {

        return vmUiService.destroyVmById(id);
    }

    @Login
    @PostMapping("/management/vm/resume")
    @ApiOperation(value = "恢复虚拟机")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID")
    })
    public ResultUtil<VmInfo> resume(@RequestParam("id") int id) {
        return vmUiService.resume(id);
    }

    @Login
    @PostMapping("/management/vm/attach/cdroom")
    @ApiOperation(value = "挂载光盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "iso", value = "光盘模版ID")
    })
    public ResultUtil<VmInfo> attachCdRoom(@RequestParam("id") int id, @RequestParam("iso") int iso) {

        return vmUiService.attachCdRoom(id, iso);
    }

    @Login
    @PostMapping("/management/vm/detach/cdroom")
    @ApiOperation(value = "取消挂载光盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID")
    })
    public ResultUtil<VmInfo> detachCdRoom(@RequestParam("id") int id) {
        return vmUiService.detachCdRoom(id);
    }

    @Login
    @PostMapping("/management/vm/attach/disk")
    @ApiOperation(value = "挂载磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "volume", value = "磁盘ID")
    })
    public ResultUtil<VolumeInfo> attachDisk(@RequestParam("id") int id, @RequestParam("volume") int volume) {
        return vmUiService.attachDisk(id, volume);
    }

    @Login
    @PostMapping("/management/vm/detach/disk")
    @ApiOperation(value = "取消挂载磁盘")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "虚拟机ID"),
            @ApiImplicitParam(name = "volume", value = "磁盘ID"),
    })
    public ResultUtil<VolumeInfo> detachDisk(@RequestParam("id") int id, @RequestParam("volume") int volume) {
        return vmUiService.detachDisk(id, volume);
    }

}

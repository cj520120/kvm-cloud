package cn.roamblue.cloud.agent.controller;

import cn.roamblue.cloud.agent.service.KvmVmService;
import cn.roamblue.cloud.agent.util.XmlUtil;
import cn.roamblue.cloud.agent.service.CommmandService;
import cn.roamblue.cloud.common.agent.VmInfoModel;
import cn.roamblue.cloud.common.agent.VmModel;
import cn.roamblue.cloud.common.agent.VmStaticsModel;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * KVM虚拟机管理
 *
 * @author chenjun
 */
@RestController
@Slf4j
public class VmController {
    @Autowired
    private KvmVmService vmService;

    @Autowired
    private CommmandService kvmQemuGuestAgentService;

    /**
     * 获取虚拟机列表
     *
     * @return
     */
    @GetMapping("/vm/list")
    public ResultUtil<List<VmInfoModel>> listVm() {
        return ResultUtil.<List<VmInfoModel>>builder().data(vmService.listVm()).build();
    }

    /**
     * 获取虚拟机信息
     *
     * @param name 虚拟机名称
     * @return
     */
    @GetMapping("/vm/info")
    public ResultUtil<VmInfoModel> getVmState(@RequestParam("name") String name) {
        return ResultUtil.<VmInfoModel>builder().data(vmService.findByName(name)).build();
    }

    /**
     * 获取虚拟统计信息
     *
     * @return
     */
    @GetMapping("/vm/list/statics")
    public ResultUtil<List<VmStaticsModel>> listVmStatics() {
        return ResultUtil.<List<VmStaticsModel>>builder().data(vmService.listVmStatics()).build();
    }

    /**
     * 重启虚拟机
     *
     * @param name 虚拟机名称
     * @return
     */
    @PostMapping("/vm/restart")
    public ResultUtil<Void> restart(@RequestParam("name") String name) {
        vmService.restart(name);
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 删除虚拟机
     *
     * @param name 虚拟机名称
     * @return
     */
    @PostMapping("/vm/destroy")
    public ResultUtil<Void> destroy(@RequestParam("name") String name) {
        vmService.destroy(name);
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 停止虚拟机
     *
     * @param name 虚拟机名称
     * @return
     */
    @PostMapping("/vm/stop")
    public ResultUtil<Void> stop(@RequestParam("name") String name) {
        vmService.stop(name);
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 修改虚拟机挂载光盘
     *
     * @param info
     * @return
     */
    @PostMapping("/vm/update/cdroom")
    public ResultUtil<Void> updateAttachCdRoom(@RequestBody VmModel.UpdateCdRoom info) {
        vmService.updateDevice(info.getName(), XmlUtil.toXml(info.getPath()));
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 修改虚拟机挂载磁盘
     *
     * @param info
     * @return
     */
    @PostMapping("/vm/update/disk")
    public ResultUtil<Void> updateAttachDisk(@RequestBody VmModel.UpdateDisk info) {
        if (info.isAttach()) {
            vmService.attachDevice(info.getName(), XmlUtil.toXml(info.getDisk()));
        } else {
            vmService.detachDevice(info.getName(), XmlUtil.toXml(info.getDisk()));
        }
        return ResultUtil.<Void>builder().build();
    }


    /**
     * 修改虚拟机挂载磁盘
     *
     * @param info
     * @return
     */
    @PostMapping("/vm/update/network")
    public ResultUtil<Void> updateAttachNetwork(@RequestBody VmModel.UpdateNetwork info) {
        if (info.isAttach()) {
            vmService.attachDevice(info.getName(), XmlUtil.toXml(info.getNetwork()));
        } else {
            vmService.detachDevice(info.getName(), XmlUtil.toXml(info.getNetwork()));
        }
        return ResultUtil.<Void>builder().build();
    }

    /**
     * 启动虚拟机
     *
     * @param info
     * @return
     */
    @PostMapping("/vm/start")
    public ResultUtil<VmInfoModel> start(@RequestBody VmModel info) {
        return ResultUtil.<VmInfoModel>builder().data(vmService.start(info)).build();
    }

    /**
     * 虚拟机执行Qemu Guest Agent
     *
     * @param name    虚拟机名称
     * @param command 执行命令
     * @param args    执行参数
     * @param timeout 超时时间(秒)
     * @return
     */
    @PostMapping("/vm/command/execute")
    public ResultUtil<Map<String, Object>> executeCommand(@RequestParam("name") String name, @RequestParam("command") String command, @RequestParam("command") String args, @RequestParam(value = "timeout", defaultValue = "10") int timeout) {
        List<String> params = new ArrayList<>();
        for (String str : args.split(" ")) {
            if (!StringUtils.isEmpty(str)) {
                params.add(str);
            }
        }
        if (params.isEmpty()) {
            return ResultUtil.<Map<String, Object>>builder().code(ErrorCode.PARAM_ERROR).build();
        }
        String commandStr = params.get(0);
        params.remove(0);
        return kvmQemuGuestAgentService.execute(name, commandStr, params, timeout);
    }

    /**
     * 虚拟机执行Qemu Guest Agent写入文件
     *
     * @param name 虚拟机名称
     * @param path 文件路径
     * @param body 文件内容
     * @return
     */
    @PostMapping("/vm/command/write/file")
    public ResultUtil<Void> writeFile(@RequestParam("name") String name, @RequestParam("path") String path, @RequestParam("body") String body) {
        return kvmQemuGuestAgentService.writeFile(name, path, body);
    }
}

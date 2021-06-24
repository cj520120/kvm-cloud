package com.roamblue.cloud.agent.controller;

import com.roamblue.cloud.agent.service.CommmandService;
import com.roamblue.cloud.agent.service.KvmVmService;
import com.roamblue.cloud.agent.util.XmlUtil;
import com.roamblue.cloud.common.agent.VmInfoModel;
import com.roamblue.cloud.common.agent.VmModel;
import com.roamblue.cloud.common.agent.VmStaticsModel;
import com.roamblue.cloud.common.bean.ResultUtil;
import com.roamblue.cloud.common.util.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
@RestController
@Api(tags = "KVM虚拟机管理")
@Slf4j
public class VmController {
    @Autowired
    private KvmVmService vmService;

    @Autowired
    private CommmandService kvmQemuGuestAgentService;

    @GetMapping("/vm/list")
    @ApiOperation(value = "获取虚拟机列表")
    public ResultUtil<List<VmInfoModel>> listVm() {
        return ResultUtil.<List<VmInfoModel>>builder().data(vmService.listVm()).build();
    }

    @GetMapping("/vm/info")
    @ApiOperation(value = "获取虚拟机信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "虚拟机名称"),
    })
    public ResultUtil<VmInfoModel> getVmState(@RequestParam("name") String name) {
        return ResultUtil.<VmInfoModel>builder().data(vmService.findByName(name)).build();
    }

    @GetMapping("/vm/list/statics")
    @ApiOperation(value = "获取虚拟统计信息")
    @ApiImplicitParams({
    })
    public ResultUtil<List<VmStaticsModel>> listVmStatics() {
        return ResultUtil.<List<VmStaticsModel>>builder().data(vmService.listVmStatics()).build();
    }

    @PostMapping("/vm/restart")
    @ApiOperation(value = "重启虚拟机")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "虚拟机名称"),

    })
    public ResultUtil<Void> restart(@RequestParam("name") String name) {
        vmService.restart(name);
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/vm/destroy")
    @ApiOperation(value = "删除虚拟机")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "虚拟机名称"),

    })
    public ResultUtil<Void> destroy(@RequestParam("name") String name) {
        vmService.destroy(name);
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/vm/stop")
    @ApiOperation(value = "停止虚拟机")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "虚拟机名称"),

    })
    public ResultUtil<Void> stop(@RequestParam("name") String name) {
        vmService.stop(name);
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/vm/update/cdroom")
    @ApiOperation(value = "修改虚拟机挂载光盘")
    public ResultUtil<Void> updateAttachCdRoom(@RequestBody VmModel.UpdateCdRoom info) {
        vmService.updateDevice(info.getName(), XmlUtil.toXml(info.getPath()));
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/vm/update/disk")
    public ResultUtil<Void> updateAttachDisk(@RequestBody VmModel.UpdateDisk info) {
        if (info.isAttach()) {
            vmService.attachDevice(info.getName(), XmlUtil.toXml(info.getDisk()));
        } else {
            vmService.detachDevice(info.getName(), XmlUtil.toXml(info.getDisk()));
        }
        return ResultUtil.<Void>builder().build();
    }

    @PostMapping("/vm/start")
    @ApiOperation(value = "启动虚拟机")
    public ResultUtil<VmInfoModel> start(@RequestBody VmModel info) {
        return ResultUtil.<VmInfoModel>builder().data(vmService.start(info)).build();
    }


    @PostMapping("/vm/command/execute")
    @ApiOperation(value = "虚拟机执行Qemu Guest Agent")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "虚拟机名称"),
            @ApiImplicitParam(name = "command", value = "执行命令", example = "ls"),
            @ApiImplicitParam(name = "timeout", value = "超时时间(秒)", defaultValue = "10"),

    })
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

    @PostMapping("/vm/command/write/file")
    @ApiOperation(value = "虚拟机执行Qemu Guest Agent写入文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "虚拟机名称"),
            @ApiImplicitParam(name = "path", value = "文件路径"),
            @ApiImplicitParam(name = "body", value = "文件内容"),

    })
    public ResultUtil<Void> writeFile(@RequestParam("name") String name, @RequestParam("path") String path, @RequestParam("body") String body) {
        return kvmQemuGuestAgentService.writeFile(name, path, body);
    }
}

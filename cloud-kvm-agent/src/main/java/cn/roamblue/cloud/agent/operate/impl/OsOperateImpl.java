package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.roamblue.cloud.agent.operate.OsOperate;
import cn.roamblue.cloud.common.agent.OsRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OsOperateImpl implements OsOperate {
    private final int MAX_DEVICE_COUNT = 10;
    private final int MIN_CD_ROOM_DEVICE_ID = 0;
    private final int MIN_DISK_DEVICE_ID = MIN_CD_ROOM_DEVICE_ID + MAX_DEVICE_COUNT;
    private final int MIN_NIC_DEVICE_ID = MIN_DISK_DEVICE_ID + MAX_DEVICE_COUNT;

    @Override
    public void start(Connect connect, OsRequest.Start request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain != null) {
             if(domain.getInfo().state== DomainInfo.DomainState.VIR_DOMAIN_RUNNING){
                 return;
             }
            domain.destroy();
        }
    }

    @Override
    public void shutdown(Connect connect, OsRequest.Shutdown request) throws Exception {
        while (true) {
            Domain domain = this.findDomainByName(connect, request.getName());
            if (domain == null) {
                break;
            }
            switch (domain.getInfo().state) {
                case VIR_DOMAIN_SHUTDOWN:
                case VIR_DOMAIN_SHUTOFF:
                    domain.destroy();
                    domain.undefine();
                default:
                    ThreadUtil.sleep(1, TimeUnit.SECONDS);
                    break;
            }
        }

    }

    @Override
    public void reboot(Connect connect, OsRequest.Reboot request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        switch (domain.getInfo().state) {
            case VIR_DOMAIN_SHUTDOWN:
            case VIR_DOMAIN_SHUTOFF:
                domain.create();
                break;
            default:
                domain.reboot(1);
                break;
        }
    }

    @Override
    public void attachCdRoom(Connect connect, OsRequest.CdRoom request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大光盘数量");
        }
        String xml = ResourceUtil.readUtf8Str("xml/cd/AttachCdRoom.xml");
        int deviceId = request.getDeviceId() + MIN_CD_ROOM_DEVICE_ID;
        xml = String.format(xml, request.getPath(), request.getDeviceId(), deviceId);
        domain.updateDeviceFlags(xml, 1);
    }

    @Override
    public void detachCdRoom(Connect connect, OsRequest.CdRoom request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大光盘数量");
        }
        String xml = ResourceUtil.readUtf8Str("xml/cd/DetachCdRoom.xml");
        int deviceId = request.getDeviceId() + MIN_CD_ROOM_DEVICE_ID;
        xml = String.format(xml, request.getPath(), request.getDeviceId(), deviceId);
        domain.updateDeviceFlags(xml, 1);
    }

    @Override
    public void attachDisk(Connect connect, OsRequest.Disk request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大磁盘数量");
        }
        String xml;
        switch (request.getBus()) {
            case OsRequest.Disk.DiskBus.VIRTIO:
                xml = ResourceUtil.readUtf8Str("xml/disk/VirtioDisk.xml");
                break;
            case OsRequest.Disk.DiskBus.IDE:
                xml = ResourceUtil.readUtf8Str("xml/disk/IdeDisk.xml");
                break;
            case OsRequest.Disk.DiskBus.SCSI:
                xml = ResourceUtil.readUtf8Str("xml/disk/ScsiDisk.xml");
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "未知的总线模式:" + request.getBus());
        }
        int deviceId = request.getDeviceId() + MIN_DISK_DEVICE_ID;
        xml = String.format(xml, request.getVolumeType(), request.getVolume(), deviceId);
        domain.attachDevice(xml);
    }

    @Override
    public void detachDisk(Connect connect, OsRequest.Disk request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大磁盘数量");
        }
        String xml = ResourceUtil.readUtf8Str("xml/disk/VirtioDisk.xml");
        int deviceId = request.getDeviceId() + MIN_DISK_DEVICE_ID;
        xml = String.format(xml, request.getVolumeType(), request.getVolume(), deviceId);
        domain.detachDevice(xml);
    }

    @Override
    public void attachNic(Connect connect, OsRequest.Nic request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大网卡数量");
        }
        String xml = ResourceUtil.readUtf8Str("xml/network/Nic.xml");
        int deviceId = request.getDeviceId() + MIN_NIC_DEVICE_ID;
        xml = String.format(xml, request.getMac(), request.getDriveType(), request.getBridgeName(), deviceId);
        domain.attachDevice(xml);
    }

    @Override
    public void detachNic(Connect connect, OsRequest.Nic request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大网卡数量");
        }
        String xml = ResourceUtil.readUtf8Str("xml/network/Nic.xml");
        int deviceId = request.getDeviceId() + MIN_NIC_DEVICE_ID;
        xml = String.format(xml, request.getMac(), request.getDriveType(), request.getBridgeName(), deviceId);
        domain.detachDevice(xml);
    }

    @Override
    public void qma(Connect connect, OsRequest.Qma request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.VM_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        List<OsRequest.Qma.QmaBody> commands=request.getCommands();
        for (OsRequest.Qma.QmaBody command : commands) {
            switch (command.getCommand()){
                case OsRequest.Qma.QmaType.WRITE_FILE:
                    OsRequest.Qma.WriteFile writeFile=new Gson().fromJson(command.getData(), OsRequest.Qma.WriteFile.class);
                    int handler = qmaOpenFile(writeFile.getFileName(), domain);
                    qmaWriteFile(writeFile.getFileBody(), domain,handler);
                    qmaCloseFile(domain,handler);
                    break;
                case OsRequest.Qma.QmaType.EXECUTE:
                    OsRequest.Qma.Execute execute=new Gson().fromJson(command.getData(), OsRequest.Qma.Execute.class);

                    qmaExecuteShell(request, domain, command, execute);
                    break;
                default:
                    throw new CodeException(ErrorCode.VM_NOT_FOUND, "不支持的QMA操作:" + command.getCommand());
            }
        }
    }



    @Override
    public void destroy(Connect connect, OsRequest.Destroy request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain != null) {
            domain.destroy();
            domain.undefine();
        }
    }

    private Domain findDomainByName(Connect connect, String name) throws Exception {
        int[] ids = connect.listDomains();

        for (int id : ids) {
            Domain domain = connect.domainLookupByID(id);
            if (Objects.equals(domain.getName(), name)) {
                return domain;
            }
        }
        String[] namesOfDefinedDomain = connect.listDefinedDomains();
        for (String stopDomain : namesOfDefinedDomain) {
            Domain domain = connect.domainLookupByName(stopDomain);
            if (Objects.equals(domain.getName(), name)) {
                return domain;
            }
        }
        return null;
    }
    private static void qmaExecuteShell(OsRequest.Qma request, Domain domain, OsRequest.Qma.QmaBody command, OsRequest.Qma.Execute execute) throws LibvirtException {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>(2);
        map.put("execute", "guest-exec");
        Map<String, Object> arguments = new HashMap<>(3);
        map.put("arguments", arguments);
        map.put("path", execute.getCommand());
        map.put("arg", execute.getArgs());
        if(null== domain.qemuAgentCommand( gson.toJson(command), request.getTimeout(), 0)){
            throw new CodeException(ErrorCode.SERVER_ERROR,"执行命令失败");
        }
    }
    private void qmaCloseFile(Domain domain, int handler) throws LibvirtException {
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-close");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("handle", handler);
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(new Gson().toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
        }
    }

    private void qmaWriteFile(String body, Domain domain, int handler) throws LibvirtException {
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-write");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("handle", handler);
        arguments.put("buf-b64", cn.hutool.core.codec.Base64.encode(body.getBytes(StandardCharsets.UTF_8)));
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(new Gson().toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
        }
    }

    private int qmaOpenFile(String path, Domain domain) throws LibvirtException {
        int handler;
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-open");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("path", path);
        arguments.put("mode", "w+");
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(new Gson().toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "执行失败");
        }
        Map<String, Object> map = new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
        handler = NumberUtil.parseInt(map.get("return").toString());
        return handler;
    }
}

package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.operate.OsOperate;
import cn.chenjun.cloud.agent.util.DomainXmlUtil;
import cn.chenjun.cloud.agent.util.VncUtil;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.libvirt.Error;
import org.libvirt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class OsOperateImpl implements OsOperate {
    @Autowired
    private ApplicationConfig applicationConfig;

    private static void qmaExecuteShell(GuestQmaRequest request, Domain domain, GuestQmaRequest.Execute execute) throws LibvirtException {
        Gson gson = new Gson();
        Map<String, Object> map = new HashMap<>(2);
        map.put("execute", "guest-exec");
        Map<String, Object> arguments = new HashMap<>(3);
        map.put("arguments", arguments);
        arguments.put("path", execute.getCommand());
        arguments.put("arg", execute.getArgs());
        String commandBody = gson.toJson(map);
        String response = domain.qemuAgentCommand(commandBody, request.getTimeout(), 0);
        Map<String, Object> result = GsonBuilderUtil.create().fromJson(response, new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
        }.getType());
        String pid = ((Map<String, Object>) result.get("return")).get("pid").toString();
        map.clear();
        arguments.clear();
        arguments.put("pid", NumberUtil.parseInt(pid));
        map.put("execute", "guest-exec-status");
        map.put("arguments", arguments);
        String statusRequest = gson.toJson(map);
        boolean isExit;
        do {
            response = domain.qemuAgentCommand(statusRequest, request.getTimeout(), 0);
            result = GsonBuilderUtil.create().fromJson(response, new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
            }.getType());
            isExit = Boolean.parseBoolean(((Map<String, Object>) result.get("return")).get("exited").toString());
            if (!isExit) {
                ThreadUtil.sleep(1, TimeUnit.SECONDS);
            } else {
                int code = NumberUtil.parseInt(((Map<String, Object>) result.get("return")).get("exitcode").toString());
                if (code != 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, "执行命令失败:domain=" + domain.getName() + "command={}" + commandBody + ".response=" + response);
                } else {
                    log.info("执行命令成功:domain={} command={} result={}", domain.getName(), execute, response);
                }
            }
        } while (!isExit);

    }

    private static int bit(final int i) {
        return 1 << i;
    }

    @Override
    public GuestInfo getGustInfo(Connect connect, GuestInfoRequest request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain != null) {
            return this.initVmResponse(domain);
        } else {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
    }

    @Override
    public List<GuestInfo> listAllGuestInfo(Connect connect) throws Exception {
        List<GuestInfo> list = new ArrayList<>();
        int[] ids = connect.listDomains();
        for (int id : ids) {
            Domain domain = connect.domainLookupByID(id);
            list.add(initVmResponse(domain));
        }
        String[] namesOfDefinedDomain = connect.listDefinedDomains();
        for (String stopDomain : namesOfDefinedDomain) {
            Domain domain = connect.domainLookupByName(stopDomain);
            list.add(initVmResponse(domain));
        }
        return list;
    }

    @Override
    public List<GuestInfo> batchGustInfo(Connect connect, List<GuestInfoRequest> batchRequest) throws Exception {
        Set<String> names = batchRequest.stream().map(GuestInfoRequest::getName).collect(Collectors.toSet());
        Map<String, GuestInfo> map = new HashMap<>(4);
        List<GuestInfo> list = new ArrayList<>();
        int[] ids = connect.listDomains();
        for (int id : ids) {
            Domain domain = connect.domainLookupByID(id);
            if (names.contains(domain.getName())) {
                map.put(domain.getName(), initVmResponse(domain));
            }
        }
        String[] namesOfDefinedDomain = connect.listDefinedDomains();
        for (String stopDomain : namesOfDefinedDomain) {
            Domain domain = connect.domainLookupByName(stopDomain);
            if (names.contains(domain.getName())) {
                map.put(domain.getName(), initVmResponse(domain));
            }
        }
        for (GuestInfoRequest request : batchRequest) {
            list.add(map.get(request.getName()));
        }
        return list;
    }

    @Override
    public void shutdown(Connect connect, GuestShutdownRequest request) throws Exception {
        while (true) {
            try {
                Domain domain = this.findDomainByName(connect, request.getName());
                if (domain == null) {
                    break;
                }
                switch (domain.getInfo().state) {
                    case VIR_DOMAIN_SHUTDOWN:
                    case VIR_DOMAIN_SHUTOFF:
                        domain.destroy();
                    default:
                        domain.shutdown();
                        ThreadUtil.sleep(1, TimeUnit.SECONDS);
                        break;
                }
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    break;
                }
                throw err;
            }
        }

    }

    @Override
    public void reboot(Connect connect, GuestRebootRequest request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
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
    public void detachCdRoom(Connect connect, OsCdRoom request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        String xml = DomainXmlUtil.buildCdXml(request);
        domain.updateDeviceFlags(xml, 1);

    }

    @Override
    public void attachCdRoom(Connect connect, OsCdRoom request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        String xml = DomainXmlUtil.buildCdXml(request);
        log.info("attachCdRoom xml={}", xml);
        domain.updateDeviceFlags(xml, 1);
    }

    @Override
    public void attachDisk(Connect connect, OsDisk request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= DomainXmlUtil.MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大磁盘数量");
        }
        String xml = DomainXmlUtil.buildDiskXml(Constant.DiskBus.VIRTIO, request);
        log.info("attachDisk xml={}", xml);
        domain.attachDevice(xml);
    }

    @Override
    public void detachDisk(Connect connect, OsDisk request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= DomainXmlUtil.MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大磁盘数量");
        }
        String xml = DomainXmlUtil.buildDiskXml(Constant.DiskBus.VIRTIO, request);
        log.info("detachDisk xml={}", xml);
        domain.detachDevice(xml);
    }

    @Override
    public void attachNic(Connect connect, OsNic request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= DomainXmlUtil.MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大网卡数量");
        }
        String xml = DomainXmlUtil.buildNicXml(applicationConfig.getNetworkType(), request);
        log.info("attachNic xml={}", xml);
        domain.attachDevice(xml);
    }

    @Override
    public void detachNic(Connect connect, OsNic request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        if (request.getDeviceId() >= DomainXmlUtil.MAX_DEVICE_COUNT) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "超过最大网卡数量");
        }
        String xml = DomainXmlUtil.buildNicXml(applicationConfig.getNetworkType(), request);
        log.info("detachNic xml={}", xml);
        domain.detachDevice(xml);
    }

    @Override
    public GuestInfo start(Connect connect, GuestStartRequest request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain != null) {
            if (domain.getInfo().state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                return this.initVmResponse(domain);
            }
            domain.destroy();
        }
        String xml = DomainXmlUtil.buildDomainXml(applicationConfig.getNetworkType(), request);
        log.info("create vm={}", xml);
        domain = connect.domainCreateXML(xml, 0);
        if (Objects.nonNull(request.getQmaRequest())) {
            long start = System.currentTimeMillis();
            Map<String, Object> map = new HashMap<>(2);
            map.put("execute", "guest-info");
            Gson gson = new Gson();
            String checkQmaReadyCommand = gson.toJson(map);
            while ((System.currentTimeMillis() - start) < TimeUnit.SECONDS.toMillis(request.getQmaRequest().getTimeout())) {
                try {
                    String response = domain.qemuAgentCommand(checkQmaReadyCommand, request.getQmaRequest().getTimeout(), 0);
                    if (!StringUtils.isEmpty(response)) {
                        break;
                    }
                } catch (Exception err) {
                    if (err instanceof LibvirtException) {
                        LibvirtException libvirtException = (LibvirtException) err;
                        if (libvirtException.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机当前未运行");
                        }
                    }
                    ThreadUtil.sleep(5, TimeUnit.SECONDS);
                }
            }
            //写入qma
            try {
                this.qmaExecute(domain, request.getQmaRequest());
            } catch (CodeException err) {
                domain.destroy();
                throw err;
            } catch (Exception err) {
                domain.destroy();
                throw new CodeException(ErrorCode.SERVER_ERROR, err.getMessage());
            }
        }
        return this.initVmResponse(domain);
    }

    @Override
    public void qma(Connect connect, GuestQmaRequest request) throws Exception {
        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        this.qmaExecute(domain, request);
    }

    @Override
    public void destroy(Connect connect, GuestDestroyRequest request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain != null) {
            domain.destroy();
        }
    }

    @Override
    public void migrate(Connect connect, GuestMigrateRequest request) throws Exception {
        Domain domain = this.findDomainByName(connect, request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        @Cleanup
        Connect toConnect = new Connect(String.format("qemu+tcp://%s/system", request.getHost()));
        long liveMigrationFlag = MigrateFlags.VIR_MIGRATE_UNDEFINE_SOURCE | MigrateFlags.VIR_MIGRATE_PEER2PEER | MigrateFlags.VIR_MIGRATE_LIVE | MigrateFlags.VIR_MIGRATE_TUNNELLED | MigrateFlags.VIR_MIGRATE_UNSAFE;
        domain.migrate(toConnect, liveMigrationFlag, null, null, 0);
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

    private void qmaExecute(Domain domain, GuestQmaRequest request) throws LibvirtException {
        List<GuestQmaRequest.QmaBody> commands = request.getCommands();
        for (GuestQmaRequest.QmaBody command : commands) {
            switch (command.getCommand()) {
                case GuestQmaRequest.QmaType.WRITE_FILE:
                    GuestQmaRequest.WriteFile writeFile = new Gson().fromJson(command.getData(), GuestQmaRequest.WriteFile.class);
                    int handler = qmaOpenFile(writeFile.getFileName(), domain);
                    try {
                        qmaWriteFile(writeFile.getFileName(), writeFile.getFileBody(), domain, handler);
                    } finally {
                        qmaCloseFile(domain, handler);
                    }
                    break;
                case GuestQmaRequest.QmaType.EXECUTE:
                    GuestQmaRequest.Execute execute = new Gson().fromJson(command.getData(), GuestQmaRequest.Execute.class);
                    qmaExecuteShell(request, domain, execute);
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的QMA操作:" + command.getCommand());
            }
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
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "关闭文件失败");
        }
    }

    private void qmaWriteFile(String path, String body, Domain domain, int handler) throws LibvirtException {
        Map<String, Object> command = new HashMap<>(2);
        command.put("execute", "guest-file-write");
        Map<String, Object> arguments = new HashMap<>(2);
        arguments.put("handle", handler);
        arguments.put("buf-b64", cn.hutool.core.codec.Base64.encode(body.getBytes(StandardCharsets.UTF_8)));
        command.put("arguments", arguments);
        String response = domain.qemuAgentCommand(new Gson().toJson(command), 10, 0);
        if (StringUtils.isEmpty(response)) {
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "写入文件失败");
        }
        log.info("写入文件成功:domain={} path={} body={} response={}", domain.getName(), path, body, response);
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
            throw new CodeException(ErrorCode.VM_COMMAND_ERROR, "打开文件失败.path=" + path);
        }
        Map<String, Object> map = new Gson().fromJson(response, new TypeToken<Map<String, Object>>() {
        }.getType());
        handler = NumberUtil.parseInt(map.get("return").toString());
        return handler;
    }

    private GuestInfo initVmResponse(Domain domain) throws LibvirtException, SAXException, DocumentException {
        DomainInfo domainInfo = domain.getInfo();
        GuestInfo info = GuestInfo.builder().name(domain.getName())
                .uuid(domain.getUUIDString())
                .maxMem(domainInfo.maxMem)
                .memory(domainInfo.memory)
                .cpuTime(domainInfo.cpuTime)
                .cpu(domainInfo.nrVirtCpu)
                .build();
        if (domainInfo.state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
            String xml = domain.getXMLDesc(0);
            info.setVnc(VncUtil.getVnc(xml));
        }
        return info;
    }

    static final class MigrateFlags {
        /**
         * live migration
         */
        static final int VIR_MIGRATE_LIVE = bit(0);

        /**
         * direct source -> dest host control channel
         */
        static final int VIR_MIGRATE_PEER2PEER = bit(1);

        /**
         * tunnel migration data over libvirtd connection
         *
         * @apiNote Note the less-common spelling that we're stuck with:
         * VIR_MIGRATE_TUNNELLED should be VIR_MIGRATE_TUNNELED
         */
        static final int VIR_MIGRATE_TUNNELLED = bit(2);

        /**
         * persist the VM on the destination
         */
        static final int VIR_MIGRATE_PERSIST_DEST = bit(3);

        /**
         * undefine the VM on the source
         */
        static final int VIR_MIGRATE_UNDEFINE_SOURCE = bit(4);

        /**
         * pause on remote side
         */
        static final int VIR_MIGRATE_PAUSED = bit(5);

        /**
         * migration with non-shared storage with full disk copy
         */
        static final int VIR_MIGRATE_NON_SHARED_DISK = bit(6);

        /**
         * migration with non-shared storage with incremental copy
         * (same base image shared between source and destination)
         */
        static final int VIR_MIGRATE_NON_SHARED_INC = bit(7);

        /**
         * protect for changing domain configuration through the
         * whole migration process; this will be used automatically
         * when supported
         */
        static final int VIR_MIGRATE_CHANGE_PROTECTION = bit(8);

        /**
         * force migration even if it is considered unsafe
         */
        static final int VIR_MIGRATE_UNSAFE = bit(9);
    }

}

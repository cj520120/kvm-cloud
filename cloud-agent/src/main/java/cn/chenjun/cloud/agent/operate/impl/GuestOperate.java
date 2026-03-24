package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.config.ApplicationConfig;
import cn.chenjun.cloud.agent.util.CloudInitHelper;
import cn.chenjun.cloud.agent.util.DomainUtil;
import cn.chenjun.cloud.agent.util.VncUtil;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.core.annotation.DispatchBind;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.libvirt.*;
import org.libvirt.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Component
public class GuestOperate {

    @Autowired
    private ApplicationConfig applicationConfig;


    private static int bit(final int i) {
        return 1 << i;
    }


    @DispatchBind(command = Constant.Command.ALL_GUEST_INFO)
    public List<GuestInfo> listAllGuestInfo(Connect connect, NoneRequest request) throws Exception {
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

    @DispatchBind(command = Constant.Command.BATCH_GUEST_INFO)

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

    @DispatchBind(command = Constant.Command.GUEST_SHUTDOWN)
    public Void shutdown(Connect connect, GuestShutdownRequest request) throws Exception {
        this.stopDomain(connect, request.getName(), request.getExpireMillis());
        return null;

    }

    @DispatchBind(command = Constant.Command.GUEST_INFO)
    public GuestInfo getGustInfo(Connect connect, GuestInfoRequest request) throws Exception {

        Domain domain = DomainUtil.findDomainByName(connect, request.getName());
        if (domain != null) {
            return this.initVmResponse(domain);
        } else {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
    }

    @DispatchBind(command = Constant.Command.GUEST_DETACH_CD_ROOM)
    public Void detachCdRoom(Connect connect, ChangeGuestCdRoomRequest request) throws Exception {

        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        log.info("detachCdRoom xml={}", request.getXml());
        domain.updateDeviceFlags(request.getXml(), 1);
        return null;

    }

    @DispatchBind(command = Constant.Command.GUEST_ATTACH_CD_ROOM)
    public Void attachCdRoom(Connect connect, ChangeGuestCdRoomRequest request) throws Exception {

        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        log.info("attachCdRoom xml={}", request.getXml());
        domain.updateDeviceFlags(request.getXml(), 1);
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_ATTACH_DISK)
    public Void attachDisk(Connect connect, ChangeGuestDiskRequest request) throws Exception {

        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        log.info("attachDisk xml={}", request.getXml());
        domain.attachDevice(request.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_DETACH_DISK)
    public Void detachDisk(Connect connect, ChangeGuestDiskRequest request) throws Exception {

        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        log.info("detachDisk xml={}", request.getXml());
        domain.detachDevice(request.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_ATTACH_NIC)
    public Void attachNic(Connect connect, ChangeGuestInterfaceRequest request) throws Exception {

        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        log.info("attachNic xml={}", request.getXml());
        domain.attachDevice(request.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_DETACH_NIC)
    public Void detachNic(Connect connect, ChangeGuestInterfaceRequest request) throws Exception {

        Domain domain = connect.domainLookupByName(request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        log.info("detachNic xml={}", request.getXml());
        domain.detachDevice(request.getXml());
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_REBOOT)
    public Void reboot(Connect connect, GuestRebootRequest request) throws Exception {

        Domain domain = DomainUtil.findDomainByName(connect, request.getName());
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
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_START)
    public GuestInfo start(Connect connect, GuestStartRequest request) throws Exception {

        this.stopDomain(connect, request.getName(), TimeUnit.MINUTES.toMillis(1));
        log.info("create vm={}", request.getXml());
        initVmResource(request.getXml());
        Domain domain = connect.domainCreateXML(request.getXml(), 0);
        if (request.isWaitCloudInit()) {
            CloudInitHelper.waitCloudInit(connect, request.getName(), request.getWaitCloudInitTimeoutSeconds());
        }
        return this.initVmResponse(domain);
    }

    /**
     * 关机操作
     *
     * @param connect
     * @param name
     * @param timeout
     * @throws Exception
     */
    private void stopDomain(Connect connect, String name, long timeoutMillis) throws Exception {
        long stopTime = System.currentTimeMillis();
        String xml = null;
        while (true) {
            try {

                Domain domain = DomainUtil.findDomainByName(connect, name);
                if (domain == null) {
                    break;
                }
                xml = domain.getXMLDesc(0);
                if (System.currentTimeMillis() - stopTime > timeoutMillis) {
                    domain.destroy();
                    try {
                        domain.undefine();
                    } catch (Exception ignored) {

                    }
                } else {
                    if (Objects.requireNonNull(domain.getInfo().state) == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                        domain.shutdown();
                        ThreadUtil.sleep(5, TimeUnit.SECONDS);
                    } else {
                        try {
                            domain.destroy();
                        } catch (Exception ignored) {

                        }
                        try{
                            if(domain.hasManagedSaveImage()>0){
                                domain.managedSaveRemove();
                            }
                        }catch (Exception ignored) {

                        }
                        try {
                            int VIR_DOMAIN_UNDEFINE_MANAGED_SAVE = 1;
                            int VIR_DOMAIN_UNDEFINE_NVRAM = 4;
                            domain.undefine(VIR_DOMAIN_UNDEFINE_MANAGED_SAVE |VIR_DOMAIN_UNDEFINE_NVRAM);
                        } catch (Exception ignored) {

                        }
                    }
                }
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    break;
                }
                throw err;
            }
        }
        if (!ObjectUtils.isEmpty(xml)) {
            cleanVmResource(xml);
        }
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

    @DispatchBind(command = Constant.Command.GUEST_MIGRATE)

    public Void migrate(Connect connect, GuestMigrateRequest request) throws Exception {

        Domain domain = DomainUtil.findDomainByName(connect, request.getName());
        if (domain == null) {
            throw new CodeException(ErrorCode.GUEST_NOT_FOUND, "虚拟机没有运行:" + request.getName());
        }
        @Cleanup
        Connect toConnect = new Connect(String.format("qemu+tcp://%s/system", request.getHost()));
        long liveMigrationFlag = MigrateFlags.VIR_MIGRATE_UNDEFINE_SOURCE | MigrateFlags.VIR_MIGRATE_PEER2PEER | MigrateFlags.VIR_MIGRATE_LIVE | MigrateFlags.VIR_MIGRATE_TUNNELLED | MigrateFlags.VIR_MIGRATE_UNSAFE;
        domain.migrate(toConnect, liveMigrationFlag, null, null, 0);
        this.stopDomain(connect, request.getName(), TimeUnit.MINUTES.toMillis(10));
        return null;
    }

    @DispatchBind(command = Constant.Command.GUEST_DESTROY)

    public Void destroy(Connect connect, GuestDestroyRequest request) throws Exception {

        Domain domain = DomainUtil.findDomainByName(connect, request.getName());
        String xml = null;
        if (domain != null) {
            xml = domain.getXMLDesc(0);
            try {
                domain.destroy();
            } catch (Exception ignored) {

            }
            try {
                domain.undefine(Domain.UndefineFlags.MANAGED_SAVE | Domain.UndefineFlags.SNAPSHOTS_METADATA);
            } catch (Exception ignored) {
            }
        }
        if (!ObjectUtils.isEmpty(xml)) {
            cleanVmResource(xml);
        }
        return null;
    }

    private void initVmResource(String xml) {
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Element channelSourceElement = (Element) doc.selectSingleNode("/domain/devices/channel/source");
            if(channelSourceElement!=null) {
                String path = channelSourceElement.attributeValue("path");
                if(!ObjectUtils.isEmpty(path)) {
                    String parentPath=new File(path).getParent();
                    FileUtil.mkdir(parentPath);
                    log.info("初始化虚拟机channel目录:{}",parentPath);
                }
            }
            Element nvramElement = (Element) doc.selectSingleNode("/domain/os/nvram");
            if(nvramElement!=null){
                String path = nvramElement.getText();
                if(!ObjectUtils.isEmpty(path)){
                    String parentPath=new File(path).getParent();
                    FileUtil.mkdir(parentPath);
                    log.info("初始化虚拟机nvram目录:{}",parentPath);
                }
            }
            Element metadataElement = (Element) doc.selectSingleNode("/domain/metadata");
            if (metadataElement != null) {
                metadataElement.addNamespace("kvm-cloud", "http://kvm-cloud.local/kvm-cloud");
                List<Node> metaNodes = metadataElement.selectNodes("kvm-cloud:fileinfo");
                for (Node metaNode : metaNodes) {
                    Element element = (Element) metaNode;
                    Element resourcePathEl = (Element) element.selectSingleNode("kvm-cloud:resource-path");
                    Element resourceContentEl = (Element) element.selectSingleNode("kvm-cloud:resource-content");
                    if (resourcePathEl != null && resourceContentEl != null) {
                        File resourceFile = new File(resourcePathEl.getText());
                        String parentPath = resourceFile.getParent();
                        log.info("初始化虚拟机meta定义的文件目录:{}", parentPath);
                        FileUtil.mkdir(parentPath);
                        String resourceContent = resourceContentEl.getText();
                        byte[] bytes = Base64.getDecoder().decode(resourceContent);
                        FileUtil.writeBytes(bytes, resourceFile);
                        log.info("初始化虚拟机meta定义的文件:{}", resourceFile.getAbsolutePath());
                    }
                }
            }
        } catch (Exception err) {
            log.error("初始化虚拟机资源异常", err);
        }
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

    private void cleanVmResource(String xml) {

        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Element channelSourceElement = (Element) doc.selectSingleNode("/domain/devices/channel/source");
            if (channelSourceElement != null) {
                String path = channelSourceElement.attributeValue("path");
                if (!ObjectUtils.isEmpty(path)) {
                    File channelFile = new File(path);
                    channelFile.deleteOnExit();
                    log.info("清理虚拟机channel文件:{}", path);
                }
            }
            Element metadataElement = (Element) doc.selectSingleNode("/domain/metadata");
            if (metadataElement != null) {
                metadataElement.addNamespace("kvm-cloud", "http://kvm-cloud.local/kvm-cloud");
                List<Node> metaNodes = metadataElement.selectNodes("kvm-cloud:fileinfo");
                for (Node metaNode : metaNodes) {

                    Element element = (Element) metaNode;
                    Element resourcePathElement = (Element) element.selectSingleNode("kvm-cloud:resource-path");
                    Element cleanupFlagElement = (Element) element.selectSingleNode("kvm-cloud:cleanup-flag");
                    if (resourcePathElement != null && cleanupFlagElement != null && Constant.Enable.YES.equalsIgnoreCase(cleanupFlagElement.getText())) {
                        File resourceFile = new File(resourcePathElement.getText());
                        if (resourceFile.exists()) {
                            FileUtil.del(resourceFile);
                        }
                        log.info("清理虚拟机meta定义的文件:{}", resourceFile.getAbsolutePath());
                    }
                }
            }
        } catch (Exception err) {
            log.error("清理虚拟机资源异常", err);
        }
    }
}

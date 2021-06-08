package com.roamblue.cloud.agent.service.impl;

import com.roamblue.cloud.agent.service.KvmVmService;
import com.roamblue.cloud.agent.util.XmlUtil;
import com.roamblue.cloud.common.agent.VmInfoModel;
import com.roamblue.cloud.common.agent.VmModel;
import com.roamblue.cloud.common.agent.VmStaticsModel;
import com.roamblue.cloud.common.error.CodeException;
import com.roamblue.cloud.common.util.ErrorCode;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.libvirt.Error;
import org.libvirt.*;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KvmVmServiceImpl extends AbstractKvmService implements KvmVmService {
    @Override
    public List<VmInfoModel> listVm() {
        return super.excute(connect -> {
            int[] ids = connect.listDomains();
            List<VmInfoModel> list = new ArrayList<>(ids.length);
            for (int id : ids) {
                Domain domain = connect.domainLookupByID(id);
                if (domain != null) {
                    list.add(initVmResponse(domain));
                }
            }
            String[] namesOfDefinedDomain = connect.listDefinedDomains();
            for (String stopDomain : namesOfDefinedDomain) {
                Domain domain = connect.domainLookupByName(stopDomain);
                list.add(initVmResponse(domain));
            }
            return list;
        });
    }

    @Override
    public List<VmStaticsModel> listVmStatics() {
        return super.excute(connect -> {
            int[] ids = connect.listDomains();
            Map<Integer, VmCurrentStaticsInfo> map = new HashMap<>();
            for (int id : ids) {
                try {
                    Domain domain = connect.domainLookupByID(id);
                    map.put(id, getVmStatics(domain));
                } catch (Exception e) {

                }
            }
            Thread.sleep(2000);
            List<VmStaticsModel> list = new ArrayList<>();
            for (Map.Entry<Integer, VmCurrentStaticsInfo> entry : map.entrySet()) {
                try {
                    Domain domain = connect.domainLookupByID(entry.getKey());
                    VmCurrentStaticsInfo prev = entry.getValue();
                    VmCurrentStaticsInfo current = getVmStatics(domain);
                    long txBytes = current.tx_bytes - prev.tx_bytes;
                    long rxBytes = current.rx_bytes - prev.rx_bytes;
                    long wdBytes = current.wr_bytes - prev.wr_bytes;
                    long rdBytes = current.rd_bytes - prev.rd_bytes;

                    float disk_time = (current.disk_nano_time - prev.disk_nano_time) / 1000000000.0f;
                    float network_time = (current.network_nano_time - prev.network_nano_time) / 1000000000.0f;
                    int nr_cores = current.cpu;

                    //首先得到一个周期差：cpu_time_diff = cpuTimenow — cpuTimet seconds ago
                    //然后根据这个差值计算实际使用率：%CPU = 100 × cpu_time_diff / (t × nr_cores × 1e9)
                    long cpu_time_diff = current.cpu_time - prev.cpu_time;
                    float cpu_time = (current.cpu_nano_time - prev.cpu_nano_time) / 1000000000.0f;
                    int usage = (int) (100.0f * cpu_time_diff / (cpu_time * nr_cores * 1e9));


                    long wt_speed = (long) (wdBytes / disk_time);
                    long rd_speed = (long) (rdBytes / disk_time);
                    long tx_speed = (long) (txBytes / network_time);
                    long rx_speed = (long) (rxBytes / network_time);
                    VmStaticsModel response = VmStaticsModel.builder().cpuUsage(usage)
                            .networkSendSpeed(tx_speed)
                            .networkReceiveSpeed(rx_speed)
                            .diskReadSpeed(rd_speed)
                            .diskWriteSpeed(wt_speed)
                            .name(domain.getName())
                            .build();
                    list.add(response);
                } catch (Exception e) {

                }
            }
            return list;
        });
    }

    private VmCurrentStaticsInfo getVmStatics(Domain domain) throws LibvirtException, DocumentException {
        VmCurrentStaticsInfo statics = new VmCurrentStaticsInfo();
        String xml = domain.getXMLDesc(0);
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(sr);
            String path = "/domain/devices/disk";
            List<Node> nodeList = doc.selectNodes(path);
            for (Node node : nodeList) {
                Element element = (Element) node;
                if (element.attributeValue("device").equals("disk")) {
                    String dev = ((Element) (element.selectSingleNode("target"))).attributeValue("dev");
                    DomainBlockStats blockStats = domain.blockStats(dev);
                    statics.rd_bytes += blockStats.rd_bytes;
                    statics.wr_bytes += blockStats.wr_bytes;
                }
                statics.disk_nano_time = System.nanoTime();
            }
            path = "/domain/devices/interface";
            nodeList = doc.selectNodes(path);
            for (Node node : nodeList) {
                Element element = (Element) node;
                String dev = ((Element) (element.selectSingleNode("target"))).attributeValue("dev");
                if (StringUtils.isNotBlank(dev)) {
                    DomainInterfaceStats interfaceStats = domain.interfaceStats(dev);
                    statics.rx_bytes += interfaceStats.rx_bytes;
                    statics.tx_bytes += interfaceStats.tx_bytes;
                }
                statics.network_nano_time = System.nanoTime();
            }
            statics.cpu_time = domain.getInfo().cpuTime;
            statics.cpu = domain.getInfo().nrVirtCpu;
            statics.cpu_nano_time = System.nanoTime();
            return statics;
        }
    }

    @Override
    public VmInfoModel findByName(String name) {
        return super.excute(connect -> {

            try {
                log.info("restart name={}", name);
                Domain domain = connect.domainLookupByName(name);
                return initVmResponse(domain);
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "虚拟机未启动");
                } else {
                    throw err;
                }
            }
        });
    }

    private VmInfoModel initVmResponse(Domain domain) throws LibvirtException, DocumentException {
        DomainInfo domainInfo = domain.getInfo();
        VmInfoModel info = VmInfoModel.builder().name(domain.getName())
                .uuid(domain.getUUIDString())
                .state(domainInfo.state)
                .maxMem(domainInfo.maxMem)
                .memory(domainInfo.memory)
                .cpuTime(domainInfo.cpuTime)
                .cpu(domainInfo.nrVirtCpu)
                .build();
        if (domainInfo.state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
            String xml = domain.getXMLDesc(0);
            info.setVnc(XmlUtil.getVnc(xml));
            info.setPassword(XmlUtil.getVncPassword(xml));
        }
        return info;
    }

    @Override
    public void restart(String name) {
        super.excute(connect -> {
            try {
                log.info("restart name={}", name);
                Domain domain = connect.domainLookupByName(name);
                domain.reboot(0);
                return null;
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "虚拟机未启动");
                } else {
                    throw err;
                }
            }
        });
    }

    @Override
    public void destroy(String name) {
        super.excute(connect -> {
            destroyDomain(name, connect);
            return null;
        });
    }

    @Override
    public void stop(String name) {
        super.excute(connect -> {
            while (true) {
                try {
                    log.info("shutdown {}", name);
                    Domain domain = connect.domainLookupByName(name);
                    if (domain.getInfo().state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                        domain.shutdown();
                    }
                    Thread.sleep(1000);
                } catch (LibvirtException err) {
                    if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                        break;
                    }
                }
            }
            return null;
        });
    }

    private void destroyDomain(String name, Connect connect) throws LibvirtException {
        int[] ids = connect.listDomains();
        for (int id : ids) {
            Domain domain = connect.domainLookupByID(id);
            if (name.equals(domain.getName())) {
                domain.destroy();
            }
        }
        String[] namesOfDefinedDomain = connect.listDefinedDomains();
        for (String stopDomain : namesOfDefinedDomain) {
            if (stopDomain.equals(name)) {
                Domain domain = connect.domainLookupByName(stopDomain);
                domain.undefine();
            }
        }
    }

    @Override
    public void attachDevice(String name, String xml) {

        super.excute(connect -> {
            try {
                log.info("attachDevice name={} xml={}", name, xml);
                Domain domain = connect.domainLookupByName(name);
                domain.attachDevice(xml);
                return null;
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "虚拟机未启动");
                } else {
                    throw err;
                }
            }
        });
    }

    @Override
    public void detachDevice(String name, String xml) {

        super.excute(connect -> {
            try {
                log.info("detachDevice name={} xml={}", name, xml);
                Domain domain = connect.domainLookupByName(name);
                domain.detachDevice(xml);
                return null;
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "虚拟机未启动");
                } else {
                    throw err;
                }
            }
        });
    }

    @Override
    public VmInfoModel start(VmModel info) {
        return super.excute(connect -> {
            try {
                Domain domain = connect.domainLookupByName(info.getName());
                domain.destroy();
            } catch (LibvirtException err) {

            }
            String xml = XmlUtil.toXml(info);
            log.info("start xml={}", xml);
            Domain domain = connect.domainCreateXML(xml, 0);
            return initVmResponse(domain);
        });
    }

    @Override
    public void updateDevice(String name, String xml) {
        super.excute(connect -> {
            try {
                log.info("updateDevice name={} xml={}", name, xml);
                Domain domain = connect.domainLookupByName(name);
                domain.updateDeviceFlags(xml, 1);
                return null;
            } catch (LibvirtException err) {
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    throw new CodeException(ErrorCode.AGENT_VM_NOT_FOUND, "虚拟机未启动");
                } else {
                    throw err;
                }
            }
        });
    }

    private class VmCurrentStaticsInfo {
        public long rd_bytes = 0L;
        public long wr_bytes = 0L;
        public long rx_bytes = 0L;
        public long tx_bytes = 0L;
        public long disk_nano_time;
        public long network_nano_time;
        public long cpu_time;
        public int cpu;
        public long cpu_nano_time;

    }
}

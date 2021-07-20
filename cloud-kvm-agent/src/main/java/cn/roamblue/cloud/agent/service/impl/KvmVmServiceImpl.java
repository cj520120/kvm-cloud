package cn.roamblue.cloud.agent.service.impl;

import cn.roamblue.cloud.agent.service.KvmVmService;
import cn.roamblue.cloud.agent.util.XmlUtil;
import cn.roamblue.cloud.common.agent.VmInfoModel;
import cn.roamblue.cloud.common.agent.VmModel;
import cn.roamblue.cloud.common.agent.VmStaticsModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.libvirt.Error;
import org.libvirt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
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
            Map<Integer, VmCurrentStaticsInfo> map = new HashMap<>(4);
            for (int id : ids) {
                try {
                    Domain domain = connect.domainLookupByID(id);
                    map.put(id, getVmStatics(domain));
                } catch (Exception e) {
                    log.info("获取Vm指标数据错误.ID={}", id, e);
                }
            }
            Thread.sleep(2000);
            List<VmStaticsModel> list = new ArrayList<>();
            for (Map.Entry<Integer, VmCurrentStaticsInfo> entry : map.entrySet()) {
                try {
                    Domain domain = connect.domainLookupByID(entry.getKey());
                    VmCurrentStaticsInfo prev = entry.getValue();
                    VmCurrentStaticsInfo current = getVmStatics(domain);
                    long txBytes = current.txBytes - prev.txBytes;
                    long rxBytes = current.rxBytes - prev.rxBytes;
                    long wdBytes = current.wrBytes - prev.wrBytes;
                    long rdBytes = current.rdBytes - prev.rdBytes;

                    float diskTime = (current.diskNanoTime - prev.diskNanoTime) / 1000000000.0f;
                    float networkTime = (current.networkNanoTime - prev.networkNanoTime) / 1000000000.0f;
                    int nrCores = current.cpu;

                    //首先得到一个周期差：cpu_time_diff = cpuTimenow — cpuTimet seconds ago
                    //然后根据这个差值计算实际使用率：%CPU = 100 × cpu_time_diff / (t × nr_cores × 1e9)
                    long cpuTimeDiff = current.cpuTime - prev.cpuTime;
                    float cpuTime = (current.cpuNanoTime - prev.cpuNanoTime) / 1000000000.0f;
                    int usage = (int) (100.0f * cpuTimeDiff / (cpuTime * nrCores * 1e9));


                    long wtSpeed = (long) (wdBytes / diskTime);
                    long rdSpeed = (long) (rdBytes / diskTime);
                    long txSpeed = (long) (txBytes / networkTime);
                    long rxSpeed = (long) (rxBytes / networkTime);
                    VmStaticsModel response = VmStaticsModel.builder().cpuUsage(usage)
                            .networkSendSpeed(txSpeed)
                            .networkReceiveSpeed(rxSpeed)
                            .diskReadSpeed(rdSpeed)
                            .diskWriteSpeed(wtSpeed)
                            .name(domain.getName())
                            .build();
                    list.add(response);
                } catch (Exception e) {

                }
            }
            return list;
        });
    }

    private VmCurrentStaticsInfo getVmStatics(Domain domain) throws LibvirtException, SAXException, DocumentException {
        VmCurrentStaticsInfo statics = new VmCurrentStaticsInfo();
        String xml = domain.getXMLDesc(0);
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);

            String path = "/domain/devices/disk";
            List<Node> nodeList = doc.selectNodes(path);
            for (Node node : nodeList) {
                Element element = (Element) node;
                if ("disk".equals(element.attributeValue("device"))) {
                    String dev = ((Element) (element.selectSingleNode("target"))).attributeValue("dev");
                    DomainBlockStats blockStats = domain.blockStats(dev);
                    statics.rdBytes += blockStats.rd_bytes;
                    statics.wrBytes += blockStats.wr_bytes;
                }
                statics.diskNanoTime = System.nanoTime();
            }
            path = "/domain/devices/interface";
            nodeList = doc.selectNodes(path);
            for (Node node : nodeList) {
                Element element = (Element) node;
                String dev = ((Element) (element.selectSingleNode("target"))).attributeValue("dev");
                if (!StringUtils.isEmpty(dev)) {
                    DomainInterfaceStats interfaceStats = domain.interfaceStats(dev);
                    statics.rxBytes += interfaceStats.rx_bytes;
                    statics.txBytes += interfaceStats.tx_bytes;
                }
                statics.networkNanoTime = System.nanoTime();
            }
            statics.cpuTime = domain.getInfo().cpuTime;
            statics.cpu = domain.getInfo().nrVirtCpu;
            statics.cpuNanoTime = System.nanoTime();
            return statics;
        }
    }

    @Override
    public VmInfoModel findByName(String name) {
        return super.excute(connect -> {
            try {
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

    private VmInfoModel initVmResponse(Domain domain) throws LibvirtException, SAXException, DocumentException {
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
                //do nothing
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
        private long rdBytes = 0L;
        private long wrBytes = 0L;
        private long rxBytes = 0L;
        private long txBytes = 0L;
        private long diskNanoTime;
        private long networkNanoTime;
        private long cpuTime;
        private int cpu;
        private long cpuNanoTime;

    }
}

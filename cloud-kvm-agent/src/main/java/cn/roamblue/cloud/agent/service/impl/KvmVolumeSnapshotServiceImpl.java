package cn.roamblue.cloud.agent.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.roamblue.cloud.agent.service.KvmVolumeSnapshotService;
import cn.roamblue.cloud.common.agent.VolumeSnapshotModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.Error;
import org.libvirt.LibvirtException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class KvmVolumeSnapshotServiceImpl extends AbstractKvmService implements KvmVolumeSnapshotService {


    @Override
    public List<VolumeSnapshotModel> listSnapshot(String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -l %s", file);
        try {
            Process process = Runtime.getRuntime().exec(command);
            String message = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
            int code = process.waitFor();
            if (code != 0) {
                throw new CodeException(ErrorCode.SERVER_ERROR, message + ".code=" + code);
            }
            List<VolumeSnapshotModel> volumeSnapshotModelList = new ArrayList<>();
            if (!StringUtils.isEmpty(message)) {
                log.info("file snapshot response:{}", message);
                String[] lines = message.split("\n");
                for (int i = 2; i < lines.length; i++) {
                    String line = lines[i];
                    List<String> list = Arrays.asList(line.split(" ")).stream().filter(t -> !StringUtils.isEmpty(t)).collect(Collectors.toList());
                    String tag = list.get(1);
                    String createTime = list.get(list.size() - 3) + " " + list.get(list.size() - 2);
                    volumeSnapshotModelList.add(VolumeSnapshotModel.builder().tag(tag).createTime(DateUtil.parse(createTime, "yyyy-MM-dd HH:mm:ss")).build());
                }
            }
            return volumeSnapshotModelList;
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        }
    }

    @Override
    public VolumeSnapshotModel createSnapshot(String vmName, String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -c %s %s", name, file);
        return super.execute(connect -> {
            Domain domain = null;
            String tempFile = "/tmp/" + UUID.randomUUID();
            if (!StringUtils.isEmpty(vmName)) {
                try {
                    domain = connect.domainLookupByName(vmName);
                    if (domain.getInfo().state != DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "当前虚拟机不是运行状态");
                    }
                    domain.save(tempFile);
                    log.info("save vm {} to {}", vmName, tempFile);
                } catch (LibvirtException err) {
                    if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                        log.info("vm {} not running", vmName);
                    } else {
                        throw err;
                    }
                }

            }
            try {
                log.info("exec command:{}", command);
                Process process = Runtime.getRuntime().exec(command);
                String message = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
                int code = process.waitFor();
                if (code != 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, message + ".code=" + code);
                }
                return VolumeSnapshotModel.builder().tag(name).createTime(new Date()).build();
            } catch (CodeException err) {
                throw err;
            } catch (Exception err) {
                log.error("exec command fail.command={}", command, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            } finally {
                if (domain != null) {
                    connect.restore(tempFile);
                    log.info("restore vm {} from {}", vmName, tempFile);
                    FileUtil.del(tempFile);
                }
            }
        });
    }

    @Override
    public void revertSnapshot(String vmName, String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -a %s %s", name, file);
        super.execute(connect -> {
            String xml = null;
            if (!StringUtils.isEmpty(vmName)) {
                try {
                    Domain domain = connect.domainLookupByName(vmName);
                    xml = domain.getXMLDesc(0);
                    domain.destroy();
                    log.info("destroy vm {}", vmName);
                } catch (LibvirtException err) {
                    if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                        log.info("vm {} not running", vmName);
                    } else {
                        throw err;
                    }
                }

            }
            try {
                log.info("exec command:{}", command);
                Process process = Runtime.getRuntime().exec(command);
                String message = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
                int code = process.waitFor();
                if (code != 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, message + ".code=" + code);
                }
                return null;
            } catch (CodeException err) {
                throw err;
            } catch (Exception err) {
                log.error("exec command fail.command={}", command, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            } finally {
                if (!StringUtils.isEmpty(xml)) {
                    connect.domainCreateXML(xml, 0);
                    log.info("start vm {}", vmName);
                }
            }
        });

    }

    @Override
    public void deleteSnapshot(String vmName, String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -d %s %s", name, file);
        super.execute(connect -> {
            Domain domain = null;
            String tempFile = "/tmp/" + UUID.randomUUID();
            if (!StringUtils.isEmpty(vmName)) {
                try {
                    domain = connect.domainLookupByName(vmName);
                    if (domain.getInfo().state != DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "当前虚拟机不是运行状态");
                    }
                    domain.save(tempFile);
                    log.info("save vm {} to {}", vmName, tempFile);
                } catch (LibvirtException err) {
                    if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                        log.info("vm {} not running", vmName);
                    } else {
                        throw err;
                    }
                }

            }
            try {
                log.info("exec command:{}", command);
                Process process = Runtime.getRuntime().exec(command);
                String message = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
                int code = process.waitFor();
                if (code != 0) {
                    throw new CodeException(ErrorCode.SERVER_ERROR, message + ".code=" + code);
                }
                return message;
            } catch (CodeException err) {
                throw err;
            } catch (Exception err) {
                log.error("exec command fail.command={}", command, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            } finally {
                if (domain != null) {
                    connect.restore(tempFile);
                    log.info("restore vm {} from {}", vmName, tempFile);
                    FileUtil.del(tempFile);
                }
            }
        });
    }


    private String getVolumePath(String storageName, String volumeName) {
        return String.format("/mnt/%s/%s", storageName, volumeName);
    }
}

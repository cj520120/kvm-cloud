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

import java.io.IOException;
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
    public List<VolumeSnapshotModel> listSnapshot(String vmName, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -l %s", file);
        try {
            String message = this.execute(command);
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
                volumeSnapshotModelList.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
            }
            return volumeSnapshotModelList;
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        }
    }

    @Override
    public VolumeSnapshotModel createSnapshot(String vmName, String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -c %s %s", name, file);
        try {
            this.saveDomainAndExecute(vmName, command);
            return VolumeSnapshotModel.builder().tag(name).createTime(new Date()).build();
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        }
    }

    @Override
    public void revertSnapshot(String vmName, String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -a %s %s", name, file);
        try {
            this.restartDomainAndExecute(vmName, command);
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        }
    }

    @Override
    public void deleteSnapshot(String vmName, String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -d %s %s", name, file);
        try {
            this.saveDomainAndExecute(vmName, command);
        } catch (CodeException err) {
            throw err;
        } catch (Exception err) {
            throw new CodeException(ErrorCode.SERVER_ERROR, err);
        }
    }

    private String execute(String command) throws IOException, InterruptedException {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            String message = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
            int code = process.waitFor();
            log.info("exec command:{},code:{},response:{}", command, code, message);
            if (code != 0) {
                throw new CodeException(ErrorCode.SERVER_ERROR, message + ".code=" + code);
            }
            return message;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private String saveDomainAndExecute(String name, String command) throws LibvirtException {
        return super.execute(connect -> {
            Domain domain = null;
            String tempFile = "/tmp/" + UUID.randomUUID();
            if (!StringUtils.isEmpty(name)) {
                try {
                    domain = connect.domainLookupByName(name);
                    if (domain.getInfo().state != DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "当前虚拟机不是运行状态");
                    }
                    domain.save(tempFile);
                    log.info("save vm {} to {}", name, tempFile);
                } catch (LibvirtException err) {
                    if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                        throw err;
                    }
                }

            }
            try {
                return this.execute(command);
            } catch (CodeException err) {
                throw err;
            } catch (Exception err) {
                log.error("exec command fail.command={}", command, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            } finally {
                if (domain != null) {
                    connect.restore(tempFile);
                    log.info("restore vm {} from {}", name, tempFile);
                    FileUtil.del(tempFile);
                }
            }
        });
    }

    private String restartDomainAndExecute(String name, String command) throws LibvirtException {
        return super.execute(connect -> {
            String xml = null;
            if (!StringUtils.isEmpty(name)) {
                try {
                    Domain domain = connect.domainLookupByName(name);
                    if (domain.getInfo().state != DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "当前虚拟机不是运行状态");
                    }
                    xml = domain.getXMLDesc(0);
                    domain.destroy();
                } catch (LibvirtException err) {
                    if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                        throw err;
                    }
                }

            }
            try {
                return this.execute(command);
            } catch (CodeException err) {
                throw err;
            } catch (Exception err) {
                log.error("exec command fail.command={}", command, err);
                throw new CodeException(ErrorCode.SERVER_ERROR, err);
            } finally {
                if (!StringUtils.isEmpty(xml)) {
                    connect.domainCreateXML(xml, 0);
                    log.info("start vm {} xml={}", name, xml);
                }
            }
        });
    }

    private String getVolumePath(String storageName, String volumeName) {
        return String.format("/mnt/%s/%s", storageName, volumeName);
    }
}

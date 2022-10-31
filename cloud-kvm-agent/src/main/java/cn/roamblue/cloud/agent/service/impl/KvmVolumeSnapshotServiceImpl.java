package cn.roamblue.cloud.agent.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.roamblue.cloud.agent.service.KvmVolumeSnapshotService;
import cn.roamblue.cloud.common.agent.VolumeSnapshotModel;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Slf4j
@Service
public class KvmVolumeSnapshotServiceImpl extends AbstractKvmService implements KvmVolumeSnapshotService {


    @Override
    public List<VolumeSnapshotModel> listSnapshot(String storage, String volume) {
        String file = this.getVolumePath(storage,volume);
        String command = String.format("qemu-img snapshot -l %s", file);
        String response = this.exec(command);
        List<VolumeSnapshotModel> volumeSnapshotModelList = new ArrayList<>();
        if (!StringUtils.isEmpty(response)) {
            log.info("file snapshot response:{}", response);
            String[] lines = response.split("\n");
            for (int i = 2; i < lines.length; i++) {
                String line = lines[i];
                List<String> list = Arrays.asList(line.split(" ")).stream().filter(t -> !StringUtils.isEmpty(t)).collect(Collectors.toList());
                String tag = list.get(1);
                String createTime = list.get(list.size() - 3) + " " + list.get(list.size() - 2);
                volumeSnapshotModelList.add(VolumeSnapshotModel.builder().tag(tag).createTime(DateUtil.parse(createTime, "yyyy-MM-dd HH:mm:ss")).build());
            }
        }
        return volumeSnapshotModelList;
    }

    @Override
    public VolumeSnapshotModel createSnapshot(String name, String storage, String volume) {
        String file = this.getVolumePath(storage,volume);
        String command = String.format("qemu-img snapshot -c %s %s", name, file);
        this.exec(command);
        List<VolumeSnapshotModel> list = this.listSnapshot(storage, volume);
        return list.stream().filter(t -> t.getTag().equals(name)).findFirst().get();
    }

    @Override
    public void revertSnapshot(String name, String storage, String volume) {
        String file = this.getVolumePath(storage,volume);
        String command = String.format("qemu-img snapshot -a %s %s", name, file);
        this.exec(command);

    }

    @Override
    public void deleteSnapshot(String name, String storage, String volume) {
        String file = this.getVolumePath(storage, volume);
        String command = String.format("qemu-img snapshot -d %s %s", name, file);
        this.exec(command);
    }

    private String exec(String command) {
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
        }
    }

    private String getVolumePath(String storageName, String volumeName) {
        return String.format("/mnt/%s/%s", storageName, volumeName);
    }
}

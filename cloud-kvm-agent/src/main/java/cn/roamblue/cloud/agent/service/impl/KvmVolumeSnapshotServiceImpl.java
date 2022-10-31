package cn.roamblue.cloud.agent.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.roamblue.cloud.agent.service.KvmVolumeSnapshotService;
import cn.roamblue.cloud.common.agent.VolumeSnapshotModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public List<VolumeSnapshotModel> listSnapshot(String file) {
        String command = String.format("qemu-img snapshot -l %s", file);
        String response = RuntimeUtil.execForStr(command).trim();
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
    public VolumeSnapshotModel createSnapshot(String name, String file) {
        String command = String.format("qemu-img snapshot -c %s %s", name, file);
        log.info("createSnapshot command={}", command);
        RuntimeUtil.execForStr(command);
        List<VolumeSnapshotModel> list = this.listSnapshot(file);
        return list.stream().filter(t -> t.getTag().equals(name)).findFirst().get();
    }

    @Override
    public void revertSnapshot(String name, String file) {
        String command = String.format("qemu-img snapshot -a %s %s", name, file);
        RuntimeUtil.execForStr(command);

    }

    @Override
    public void deleteSnapshot(String name, String file) {
        String command = String.format("qemu-img snapshot -d %s %s", name, file);
        RuntimeUtil.execForStr(command);
    }
}

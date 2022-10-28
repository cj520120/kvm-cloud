package cn.roamblue.cloud.agent.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.roamblue.cloud.agent.service.KvmVolumeService;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.agent.VolumeSnapshotModel;
import lombok.extern.slf4j.Slf4j;
import org.anarres.qemu.image.QEmuImage;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;
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
public class KvmVolumeServiceImpl extends AbstractKvmService implements KvmVolumeService {
    @Override
    public List<VolumeModel> listVolume(String storageName) {
        return super.execute(connect -> {
            StoragePool storagePool = connect.storagePoolLookupByName(storageName);
            storagePool.refresh(0);
            String[] volumes = storagePool.listVolumes();
            List<VolumeModel> list = new ArrayList<>();
            for (String volume : volumes) {
                list.add(getVolume(storageName, volume));
            }
            return list;
        });
    }

    @Override
    public VolumeModel getVolume(String storageName, String volumeName) {
        return super.execute(connect -> {
            StoragePool storagePool = connect.storagePoolLookupByName(storageName);
            try {
                StorageVol storageVol = storagePool.storageVolLookupByName(volumeName);

                StorageVolInfo storageVolInfo = storageVol.getInfo();

                String type = null;
                if (storageVolInfo != null && storageVolInfo.type != null) {
                    type = storageVolInfo.type.toString();
                }
                return VolumeModel.builder().storage(storageName)
                        .name(volumeName)
                        .type(type)
                        .path(storageVol.getPath())
                        .capacity(storageVolInfo.capacity)
                        .allocation(storageVolInfo.allocation)
                        .build();
            } catch (Exception e) {
                storagePool.refresh(0);
                throw e;
            }
        });
    }

    @Override
    public VolumeModel reSize(String storageName, String volumeName, long size) {
        QEmuImage img;

        return super.execute(connect -> {
            StoragePool storagePool = connect.storagePoolLookupByName(storageName);
            StorageVol storageVol = storagePool.storageVolLookupByName(volumeName);
            storageVol.resize(size, 0);
            StorageVolInfo storageVolInfo = storageVol.getInfo();
            return VolumeModel.builder().storage(storageName)
                    .name(volumeName)
                    .type(storageVolInfo.type.toString())
                    .path(storageVol.getPath())
                    .capacity(storageVolInfo.capacity)
                    .allocation(storageVolInfo.allocation)
                    .build();
        });
    }

    @Override
    public void destroyVolume(String storage, String volume) {
        try {
            super.execute(connect -> {
                StoragePool storagePool = connect.storagePoolLookupByName(storage);
                StorageVol storageVol = storagePool.storageVolLookupByName(volume);
                storageVol.wipe();
                storageVol.delete(0);
                return null;
            });
            log.info("destroy volume.storage={} volume={}", storage, volume);
        } catch (Exception err) {
            log.error("destroy volume fail.storage={} volume={}", storage, volume, err);
        }
    }

    @Override
    public VolumeModel createVolume(String storage, String volume, String path, long capacityGb, String backingVolume) {

        return super.execute(connect -> {
            StringBuilder sb = new StringBuilder();
            sb.append("<volume type='file'>")
                    .append("<name>").append(volume).append("</name>")
                    .append("<source></source>");
            if (capacityGb > 0) {
                sb.append("<capacity unit='GiB'>").append(capacityGb).append("</capacity>");
                sb.append("<allocation>0</allocation>");
            }
            sb.append("<target>")
                    .append("<path>").append(path).append("</path>")
                    .append("<format type='qcow2'/>")
                    .append("<permissions>")
                    .append("<mode>0600</mode>")
                    .append("<owner>0</owner>")
                    .append("<group>0</group>")
                    .append("</permissions>")
                    .append("</target>");
            if (!StringUtils.isEmpty(backingVolume)) {
                sb.append("<backingStore>")
                        .append("<path>").append(backingVolume).append("</path>")
                        .append("<format type='qcow2'/>")
                        .append("<permissions>")
                        .append("<mode>0600</mode>")
                        .append("<owner>107</owner>")
                        .append("<group>107</group>")
                        .append("</permissions>")
                        .append("</backingStore>");
            }
            sb.append("</volume>");
            StoragePool storagePool = connect.storagePoolLookupByName(storage);
            StorageVol storageVol = storagePool.storageVolCreateXML(sb.toString(), 0);
            StorageVolInfo storageVolInfo = storageVol.getInfo();
            storagePool.refresh(0);
            log.info("create volume.storage={} volume={} xml={}", storage, volume, sb.toString());
            return VolumeModel.builder().storage(storage)
                    .name(volume)
                    .path(storageVol.getPath())
                    .type(storageVolInfo.type.toString())
                    .capacity(storageVolInfo.capacity)
                    .allocation(storageVolInfo.allocation)
                    .build();
        });
    }

    @Override
    public VolumeModel cloneVolume(String sourceStorage, String sourceVolume, String targetStorage, String targetVolume, String path) {
        return super.execute(connect -> {
            StoragePool sourceStoragePool = connect.storagePoolLookupByName(sourceStorage);
            StoragePool targetStoragePool = connect.storagePoolLookupByName(targetStorage);
            StorageVol sourceVol = sourceStoragePool.storageVolLookupByName(sourceVolume);
            StringBuilder sb = new StringBuilder();
            sb.append("<volume type='file'>")
                    .append("<name>").append(targetVolume).append("</name>")
                    .append("<source></source>")
                    .append("<target>")
                    .append("<path>").append(path).append("</path>")
                    .append("<format type='qcow2'/>")
                    .append("<permissions>")
                    .append("<mode>0600</mode>")
                    .append("<owner>107</owner>")
                    .append("<group>107</group>")
                    .append("</permissions>")
                    .append("</target>")
                    .append("</volume>");

            StorageVol targetVol = targetStoragePool.storageVolCreateXMLFrom(sb.toString(), sourceVol, 0);
            StorageVolInfo storageVolInfo = targetVol.getInfo();
            return VolumeModel.builder().storage(targetStorage)
                    .name(targetVolume)
                    .type(storageVolInfo.type.toString())
                    .path(targetVol.getPath())
                    .capacity(storageVolInfo.capacity)
                    .allocation(storageVolInfo.allocation)
                    .build();
        });
    }

    @Override
    public List<VolumeSnapshotModel> listSnapshot(String file) {
        String command = String.format("qemu-img snapshot -l %s", file);
        String response = RuntimeUtil.execForStr(command).trim();
        List<VolumeSnapshotModel> volumeSnapshotModelList = new ArrayList<>();
        if (!StringUtils.isEmpty(response)) {
            String[] lines = response.split("\n");
            for (int i = 2; i < lines.length; i++) {
                String line=lines[i];
                List<String> list = Arrays.asList(line.split(" ")).stream().filter(t -> !StringUtils.isEmpty(t)).collect(Collectors.toList());
                String tag = list.get(1);
                String createTime = list.get(list.size() - 3) + " " + list.get(list.size() - 2);
                System.out.println(line);
                volumeSnapshotModelList.add(VolumeSnapshotModel.builder().tag(tag).createTime(DateUtil.parse(createTime, "yyyy-MM-dd HH:mm:ss")).build());
            }
        }
        return volumeSnapshotModelList;
    }

    @Override
    public VolumeSnapshotModel createSnapshot(String name, String file) {
        String command = String.format("qemu-img snapshot -c %s %s", name, file);
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

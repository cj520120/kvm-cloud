package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.VolumeOperate;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpUtil;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class VolumeOperateImpl implements VolumeOperate {
    @Synchronized
    private StoragePool getStorage(Connect connect,String name)throws Exception{
        StoragePool storagePool = connect.storagePoolLookupByName(name);
        if(storagePool!=null) {
            try {
                storagePool.refresh(0);
            } catch (Exception err) {

            }
        }
        return storagePool;
    }
    @Override
    public VolumeInfo getInfo(Connect connect, VolumeInfoRequest request) throws Exception {
        StoragePool storagePool = this.getStorage(connect,request.getSourceStorage());
        String[] names = storagePool.listVolumes();
        StorageVol findVol = null;
        for (String name : names) {
            StorageVol storageVol = storagePool.storageVolLookupByName(name);
            if (Objects.equals(storageVol.getPath(), request.getSourceVolume())) {
                findVol = storageVol;
                break;
            }
        }
        if (findVol == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在:" + request.getSourceName());
        }
        StorageVolInfo storageVolInfo = findVol.getInfo();
        VolumeInfo volume = VolumeInfo.builder().storage(request.getSourceStorage())
                .name(request.getSourceName())
                .path(findVol.getPath())
                .type(storageVolInfo.type.toString())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
        this.init(findVol, volume);
        return volume;
    }

    @Override
    public List<VolumeInfo> batchInfo(Connect connect, List<VolumeInfoRequest> batchRequest) throws Exception {
        Map<String, List<VolumeInfoRequest>> list = batchRequest.stream().collect(Collectors.groupingBy(VolumeInfoRequest::getSourceStorage));
        Map<String, Map<String, VolumeInfo>> result = new HashMap<>(4);

        for (Map.Entry<String, List<VolumeInfoRequest>> entry : list.entrySet()) {
            String storage = entry.getKey();
            Map<String, VolumeInfo> map = new HashMap<>(4);
            Set<String> volumePaths = entry.getValue().stream().map(VolumeInfoRequest::getSourceVolume).collect(Collectors.toSet());
            StoragePool storagePool = this.getStorage(connect,storage);

            String[] names = storagePool.listVolumes();
            for (String name : names) {
                StorageVol storageVol = storagePool.storageVolLookupByName(name);
                if (volumePaths.contains(storageVol.getPath())) {
                    StorageVolInfo storageVolInfo = storageVol.getInfo();
                    VolumeInfo volume = VolumeInfo.builder().storage(storage)
                            .name("")
                            .path(storageVol.getPath())
                            .type(storageVolInfo.type.toString())
                            .capacity(storageVolInfo.capacity)
                            .allocation(storageVolInfo.allocation)
                            .build();
                    this.init(storageVol, volume);
                    map.put(storageVol.getPath(), volume);
                }
            }
            result.put(storage, map);
        }
        List<VolumeInfo> modelList = new ArrayList<>();
        for (VolumeInfoRequest request : batchRequest) {
            VolumeInfo model = result.get(request.getSourceStorage()).get(request.getSourceVolume());
            if (model != null) {
                model.setName(request.getSourceName());
            }
            modelList.add(model);
        }
        return modelList;
    }

    @Override
    public VolumeInfo create(Connect connect, VolumeCreateRequest request) throws Exception {
        String xml;
        if (StringUtils.isEmpty(request.getParentVolume())) {
            xml = ResourceUtil.readUtf8Str("xml/volume/CreateVolume.xml");
            xml = String.format(xml, request.getTargetName(), request.getTargetSize(), request.getTargetSize(), request.getTargetType());
        } else {
            boolean checkParentSupport = request.getParentType().equals(Constant.VolumeType.QCOW) || request.getParentType().equals(Constant.VolumeType.QCOW2) || request.getParentType().equals(Constant.VolumeType.RAW);
            boolean checkChildSupport = request.getTargetType().equals(Constant.VolumeType.QCOW) || request.getTargetType().equals(Constant.VolumeType.QCOW2);
            boolean checkSupport = checkParentSupport && checkChildSupport;
            if (!checkSupport) {
                VolumeCloneRequest volumeCloneRequest = VolumeCloneRequest.builder()
                        .sourceStorage(request.getParentStorage())
                        .sourceVolume(request.getParentVolume())
                        .targetStorage(request.getTargetStorage())
                        .targetVolume(request.getTargetVolume())
                        .targetName(request.getTargetName())
                        .targetType(request.getTargetType())
                        .build();
                return this.clone(connect, volumeCloneRequest);
            }
            xml = ResourceUtil.readUtf8Str("xml/volume/CreateVolumeByBackingStore.xml");
            xml = String.format(xml, request.getTargetName(), request.getTargetSize(), request.getTargetSize(), request.getTargetType(), request.getParentVolume(), request.getParentType());
        }
        log.info("create volume:{}",xml);
        FileUtil.mkParentDirs(request.getTargetVolume());
        FileUtil.del(request.getTargetVolume());
        StoragePool storagePool = this.getStorage(connect, request.getTargetStorage());
        storagePool.refresh(0);
        StorageVol storageVol = storagePool.storageVolCreateXML(xml, 0);
        StorageVolInfo storageVolInfo = storageVol.getInfo();
        VolumeInfo volumeInfo = VolumeInfo.builder().storage(request.getTargetStorage())
                .name(request.getTargetName())
                .path(storageVol.getPath())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
        this.init(storageVol, volumeInfo);
        return volumeInfo;
    }


    @Override
    public void destroy(Connect connect, VolumeDestroyRequest request) throws Exception {
        StoragePool storagePool = this.getStorage(connect,request.getSourceStorage());
        String[] names = storagePool.listVolumes();
        for (String name : names) {
            StorageVol storageVol = storagePool.storageVolLookupByName(name);
            if (Objects.equals(storageVol.getPath(), request.getSourceVolume())) {
                storageVol.delete(0);
                break;
            }
        }
    }

    @Override
    public VolumeInfo resize(Connect connect, VolumeResizeRequest request) throws Exception {

        StoragePool storagePool = this.getStorage(connect,request.getSourceStorage());
        StorageVol findVol = this.findVol(storagePool, request.getSourceVolume());
        if (findVol == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘不存在:" + request.getSourceVolume());
        }
        findVol.resize(request.getSize(), 0);
        StorageVolInfo storageVolInfo = findVol.getInfo();
        VolumeInfo volumeInfo = VolumeInfo.builder().storage(request.getSourceStorage())
                .name(findVol.getName())
                .type(storageVolInfo.type.toString())
                .path(findVol.getPath())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
        this.init(findVol, volumeInfo);
        return volumeInfo;
    }

    @Override
    public VolumeInfo snapshot(Connect connect, VolumeCreateSnapshotRequest request) throws Exception {
        return clone(connect, VolumeCloneRequest.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceVolume(request.getSourceVolume())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetVolume(request.getTargetVolume())
                .targetType(request.getTargetType())
                .build());
    }

    @Override
    public VolumeInfo template(Connect connect, VolumeCreateTemplateRequest request) throws Exception {

        return clone(connect, VolumeCloneRequest.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceVolume(request.getSourceVolume())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetVolume(request.getTargetVolume())
                .targetType(request.getTargetType())
                .build());
    }

    @Override
    public VolumeInfo download(Connect connect, VolumeDownloadRequest request) throws Exception {
        FileUtil.mkParentDirs(request.getTargetVolume());
        String tempFile = request.getTargetVolume() + ".data";
        try {
            FileUtil.del(tempFile);
            HttpUtil.downloadFile(request.getSourceUri(), new File(tempFile));
            FileUtil.copy(tempFile,request.getTargetVolume(),true);
            return this.getInfo(connect, VolumeInfoRequest.builder().sourceStorage(request.getTargetStorage()).sourceVolume(request.getTargetVolume()).sourceName(request.getTargetName()).build());
        } finally {
            FileUtil.del(tempFile);
        }
    }

    @Override
    public VolumeInfo migrate(Connect connect, VolumeMigrateRequest request) throws Exception {
        return clone(connect, VolumeCloneRequest.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceVolume(request.getSourceVolume())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetVolume(request.getTargetVolume())
                .targetType(request.getTargetType())
                .build());
    }

    private StorageVol findVol(StoragePool storagePool, String volume) throws Exception {
        String[] names = storagePool.listVolumes();
        StorageVol sourceVol = null;
        for (String name : names) {
            try {
                StorageVol storageVol = storagePool.storageVolLookupByName(name);
                if (volume.startsWith(storageVol.getPath())) {
                    if (Objects.equals(volume, storageVol.getPath())) {
                        sourceVol = storageVol;
                    }
                    break;
                }
            } catch (Exception err) {
                //do
            }
        }
        return sourceVol;
    }

    @Override
    public VolumeInfo clone(Connect connect, VolumeCloneRequest request) throws Exception {
        StoragePool sourceStoragePool = this.getStorage(connect, request.getSourceStorage());
        StoragePool targetStoragePool = this.getStorage(connect, request.getTargetStorage());
        StorageVol sourceVol = this.findVol(sourceStoragePool, request.getSourceVolume());
        String xml = ResourceUtil.readUtf8Str("xml/volume/CloneVolume.xml");
        FileUtil.mkParentDirs(request.getTargetVolume());
        FileUtil.del(request.getTargetVolume());
        xml = String.format(xml, request.getTargetName(), request.getTargetType());
        log.info("clone volume:{}", xml);
        StorageVol targetVol = targetStoragePool.storageVolCreateXMLFrom(xml, sourceVol, 0);
        StorageVolInfo storageVolInfo = targetVol.getInfo();

        VolumeInfo volumeInfo = VolumeInfo.builder().storage(request.getTargetStorage())
                .name(request.getTargetName())
                .type(storageVolInfo.type.toString())
                .path(targetVol.getPath())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
        this.init(targetVol, volumeInfo);
        return volumeInfo;
    }

    private void init(StorageVol vol, VolumeInfo info) throws Exception {
        String xml = vol.getXMLDesc(0);
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Element backingStoreNode = (Element) doc.selectSingleNode("/volume/backingStore/path");
            if (backingStoreNode != null) {
                info.setBackingPath(backingStoreNode.getText());
            }
            Element formatNode = (Element) doc.selectSingleNode("/volume/target/format");
            if (formatNode != null) {
                info.setType(formatNode.attributeValue("type"));
            }
            Element pathNode = (Element) doc.selectSingleNode("/volume/target/path");
            if (backingStoreNode != null) {
                info.setPath(pathNode.getText());
            } else {
                info.setBackingPath("");
            }
        }

    }
}

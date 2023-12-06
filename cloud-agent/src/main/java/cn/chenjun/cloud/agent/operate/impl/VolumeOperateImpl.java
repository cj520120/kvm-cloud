package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.VolumeOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpUtil;
import com.hubspot.jinjava.Jinjava;
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
    private StoragePool getStorage(Connect connect, String name) {
        try {
            StoragePool storagePool = connect.storagePoolLookupByName(name);
            synchronized (name.intern()) {
                try {
                    storagePool.refresh(0);
                } catch (Exception ignored) {

                }
            }
            return storagePool;
        } catch (Exception ignored) {
            return null;
        }
    }

    @DispatchBind(command = Constant.Command.VOLUME_INFO)
    @Override
    public VolumeInfo getInfo(Connect connect, VolumeInfoRequest request) throws Exception {
        StoragePool storagePool = this.getStorage(connect, request.getSourceStorage());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getSourceStorage());
        }
        StorageVol findVol = this.findVol(storagePool, request.getSourceName());
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

    @DispatchBind(command = Constant.Command.BATCH_VOLUME_INFO)
    @Override
    public List<VolumeInfo> batchInfo(Connect connect, List<VolumeInfoRequest> batchRequest) throws Exception {
        Map<String, List<VolumeInfoRequest>> list = batchRequest.stream().collect(Collectors.groupingBy(VolumeInfoRequest::getSourceStorage));
        Map<String, Map<String, VolumeInfo>> result = new HashMap<>(4);

        for (Map.Entry<String, List<VolumeInfoRequest>> entry : list.entrySet()) {
            String storage = entry.getKey();
            Map<String, VolumeInfo> map = new HashMap<>(4);
            Set<String> volumeNameList = entry.getValue().stream().map(VolumeInfoRequest::getSourceName).collect(Collectors.toSet());
            StoragePool storagePool = this.getStorage(connect, storage);
            if (storagePool != null) {
                String[] names = storagePool.listVolumes();
                for (String name : names) {
                    if (volumeNameList.contains(name)) {
                        StorageVol storageVol = storagePool.storageVolLookupByName(name);
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
            }
            result.put(storage, map);
        }
        List<VolumeInfo> modelList = new ArrayList<>();
        for (VolumeInfoRequest request : batchRequest) {
            VolumeInfo model = result.get(request.getSourceStorage()).get(request.getSourceName());
            if (model != null) {
                model.setName(request.getSourceName());
            }
            modelList.add(model);
        }
        return modelList;
    }

    @DispatchBind(command = Constant.Command.VOLUME_CREATE)
    @Override
    public VolumeInfo create(Connect connect, VolumeCreateRequest request) throws Exception {
        Map<String, Object> map = new HashMap<>(4);
        map.put("name", request.getTargetName());
        map.put("capacity", request.getTargetSize());
        map.put("allocation", request.getTargetSize());
        map.put("format", request.getTargetType());
        String xml = ResourceUtil.readUtf8Str("tpl/volume.xml");
        Jinjava jinjava = new Jinjava();
        xml = jinjava.render(xml, map);
        log.info("create volume xml={}", xml);
        StoragePool storagePool = this.getStorage(connect, request.getTargetStorage());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getTargetStorage());
        }
        StorageVol targetVol = this.findVol(storagePool, request.getTargetName());
        if (targetVol != null) {
            targetVol.delete(0);
        }
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


    @DispatchBind(command = Constant.Command.VOLUME_DESTROY)
    @Override
    public Void destroy(Connect connect, VolumeDestroyRequest request) throws Exception {
        StoragePool storagePool = this.getStorage(connect, request.getSourceStorage());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getSourceStorage());
        }
        StorageVol storageVol = this.findVol(storagePool, request.getSourceName());
        if (storageVol != null) {
            storageVol.delete(0);
        }
        return null;
    }

    @DispatchBind(command = Constant.Command.VOLUME_RESIZE)
    @Override
    public VolumeInfo resize(Connect connect, VolumeResizeRequest request) throws Exception {

        StoragePool storagePool = this.getStorage(connect, request.getSourceStorage());
        StorageVol findVol = this.findVol(storagePool, request.getSourceName());
        if (findVol == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘不存在:" + request.getSourceName());
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

    @DispatchBind(command = Constant.Command.VOLUME_SNAPSHOT)
    @Override
    public VolumeInfo snapshot(Connect connect, VolumeCreateSnapshotRequest request) throws Exception {
        return clone(connect, VolumeCloneRequest.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceName(request.getSourceName())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetType(request.getTargetType())
                .build());
    }

    @DispatchBind(command = Constant.Command.VOLUME_TEMPLATE)
    @Override
    public VolumeInfo template(Connect connect, VolumeCreateTemplateRequest request) throws Exception {

        return clone(connect, VolumeCloneRequest.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceName(request.getSourceName())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetType(request.getTargetType())
                .build());
    }

    @DispatchBind(command = Constant.Command.VOLUME_DOWNLOAD)
    @Override
    public VolumeInfo download(Connect connect, VolumeDownloadRequest request) {
        StoragePool targetStoragePool = this.getStorage(connect, request.getTargetStorage());
        if (targetStoragePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getTargetStorage());
        }
        File tempFile = FileUtil.createTempFile();
        File targetFile = null;
        try {
            HttpUtil.downloadFile(request.getSourceUri(), tempFile);
            VolumeInfo volumeInfo = this.create(connect, VolumeCreateRequest.builder()
                    .targetStorage(request.getTargetStorage())
                    .targetName(request.getTargetName())
                    .targetSize(tempFile.length() / 1024)
                    .targetType(request.getTargetType()).build());
            targetFile = new File(volumeInfo.getPath());
            FileUtil.copy(tempFile, new File(volumeInfo.getPath()), true);
            return this.getInfo(connect, VolumeInfoRequest.builder().sourceStorage(request.getTargetStorage()).sourceName(request.getTargetName()).build());
        } catch (Exception err) {
            if (targetFile != null) {
                FileUtil.del(targetFile);
            }
            log.error("下载导入模版出错.", err);
            throw new CodeException(ErrorCode.SERVER_ERROR, "下载文件出错:" + err.getMessage());
        } finally {
            FileUtil.del(tempFile);
        }
    }

    @DispatchBind(command = Constant.Command.VOLUME_MIGRATE)
    @Override
    public VolumeInfo migrate(Connect connect, VolumeMigrateRequest request) throws Exception {
        return clone(connect, VolumeCloneRequest.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceName(request.getSourceName())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetType(request.getTargetType())
                .build());
    }

    private StorageVol findVol(StoragePool storagePool, String volume) {
        try {
            return storagePool.storageVolLookupByName(volume);
        } catch (Exception err) {
            return null;
        }
    }

    @DispatchBind(command = Constant.Command.VOLUME_CLONE)
    @Override
    public VolumeInfo clone(Connect connect, VolumeCloneRequest request) throws Exception {
        StoragePool sourceStoragePool = this.getStorage(connect, request.getSourceStorage());
        if (sourceStoragePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getSourceStorage());
        }
        StoragePool targetStoragePool = this.getStorage(connect, request.getTargetStorage());
        if (targetStoragePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getTargetStorage());
        }
        StorageVol sourceVol = this.findVol(sourceStoragePool, request.getSourceName());
        StorageVol targetVol = this.findVol(targetStoragePool, request.getTargetName());
        if (targetVol != null) {
            targetVol.delete(0);
        }
        String xml = ResourceUtil.readUtf8Str("tpl/volume.xml");
        Map<String, Object> map = new HashMap<>(2);
        map.put("name", request.getTargetName());
        map.put("format", request.getTargetType());
        Jinjava jinjava = new Jinjava();
        xml = jinjava.render(xml, map);
        log.info("clone volume xml={}", xml);
        targetVol = targetStoragePool.storageVolCreateXMLFrom(xml, sourceVol, 0);
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

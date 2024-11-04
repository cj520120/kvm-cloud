package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.operate.VolumeOperate;
import cn.chenjun.cloud.agent.operate.annotation.DispatchBind;
import cn.chenjun.cloud.agent.util.StorageUtil;
import cn.chenjun.cloud.agent.util.TemplateUtil;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.libvirt.Connect;
import org.libvirt.LibvirtUtil;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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


    @DispatchBind(command = Constant.Command.VOLUME_INFO)
    @Override
    public VolumeInfo getInfo(Connect connect, VolumeInfoRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getSourceStorage());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getSourceStorage());
        }
        StorageVol findVol = this.findVol(storagePool, request.getSourceName());
        if (findVol == null) {
            throw new CodeException(ErrorCode.VOLUME_NOT_FOUND, "磁盘不存在:" + request.getSourceName());
        }

        LibvirtUtil.StorageVolInfo storageVolInfo = LibvirtUtil.getVolInfo(findVol);
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
            StoragePool storagePool = StorageUtil.findStorage(connect, storage);
            if (storagePool != null) {
                String[] names = storagePool.listVolumes();
                for (String name : names) {
                    if (volumeNameList.contains(name)) {
                        StorageVol storageVol = storagePool.storageVolLookupByName(name);
                        LibvirtUtil.StorageVolInfo storageVolInfo = LibvirtUtil.getVolInfo(storageVol);
                        VolumeInfo volume = VolumeInfo.builder().storage(storage)
                                .name("")
                                .path(storageVol.getPath())
                                .type(storageVolInfo.type.toString())
                                .capacity(storageVolInfo.capacity)
                                .allocation(storageVolInfo.allocation)
                                .build();
                        this.init(storageVol, volume);
                        map.put(name, volume);
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
        Jinjava jinjava = TemplateUtil.create();
        xml = jinjava.render(xml, map);
        log.info("create volume xml={}", xml);
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getTargetStorage());
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getTargetStorage());
        }
        StorageVol targetVol = this.findVol(storagePool, request.getTargetName());
        if (targetVol != null) {
            targetVol.delete(0);
        }
        StorageVol storageVol = storagePool.storageVolCreateXML(xml, 0);
        LibvirtUtil.StorageVolInfo storageVolInfo = LibvirtUtil.getVolInfo(storageVol);

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
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getSourceStorage());
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

        StoragePool storagePool = StorageUtil.findStorage(connect, request.getSourceStorage());
        StorageVol findVol = this.findVol(storagePool, request.getSourceName());
        if (findVol == null) {
            throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘不存在:" + request.getSourceName());
        }
        findVol.resize(request.getSize(), 0);
        LibvirtUtil.StorageVolInfo storageVolInfo = LibvirtUtil.getVolInfo(findVol);
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
        StoragePool targetStoragePool = StorageUtil.findStorage(connect, request.getTargetStorage());
        if (targetStoragePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getTargetStorage());
        }
        String targetFilePath = "/mnt/" + request.getTargetStorage() + "/" + request.getTargetName();
        File targetFile = new File(targetFilePath);
        try {
            HttpUtil.downloadFile(request.getSourceUri(), targetFile, new StreamProgress() {
                int lastPercent = 0;

                @Override
                public void start() {
                    log.info("开始下载文件:uri={},file={}", request.getSourceUri(), targetFilePath);
                }

                @Override
                public void progress(long total, long progressSize) {
                    if (total > 0) {
                        int percent = (int) (progressSize * 100 / total);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            log.info("{} 下载进度 {}%", targetFilePath, lastPercent);
                        }
                    }
                }

                @Override
                public void finish() {
                    log.info("文件下载完毕:{}", targetFilePath);

                }
            });
            if (targetFile.length() <= 0) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "文件下载错误，大小为0");
            }
            if (!ObjectUtils.isEmpty(request.getMd5())) {
                log.info("开始计算文件MD5:{}", targetFilePath);
                String md5 = DigestUtil.md5Hex(targetFile);
                log.info("文件md5:{}={}", md5, targetFilePath);
                if (!md5.equalsIgnoreCase(request.getMd5())) {
                    log.info("文件检验md5失败，下载失败.file={},fileMd5={},checkMd5={}", targetFilePath, md5, request.getMd5());
                    throw new CodeException(ErrorCode.SERVER_ERROR, "文件下载MD5错误");
                }
                log.info("下载文件md5检验通过:{}", targetFilePath);
            }
            return this.getInfo(connect, VolumeInfoRequest.builder().sourceStorage(request.getTargetStorage()).sourceName(request.getTargetName()).build());
        } catch (Exception err) {
            FileUtil.del(targetFile);
            log.error("下载导入模版出错.", err);
            throw new CodeException(ErrorCode.SERVER_ERROR, "下载文件出错:" + err.getMessage());
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
        StoragePool sourceStoragePool = StorageUtil.findStorage(connect, request.getSourceStorage());
        if (sourceStoragePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getSourceStorage());
        }
        StoragePool targetStoragePool = StorageUtil.findStorage(connect, request.getTargetStorage());
        if (targetStoragePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getTargetStorage());
        }
        StorageVol sourceVol = this.findVol(sourceStoragePool, request.getSourceName());
        StorageVol targetVol = this.findVol(targetStoragePool, request.getTargetName());
        if (targetVol != null) {
            targetVol.delete(0);
        }
        String xml = ResourceUtil.readUtf8Str("tpl/volume.xml");
        LibvirtUtil.StorageVolInfo storageVolInfo = LibvirtUtil.getVolInfo(sourceVol);
        Map<String, Object> map = new HashMap<>(2);
        map.put("name", request.getTargetName());
        map.put("format", request.getTargetType());
        map.put("capacity", storageVolInfo.capacity);
        Jinjava jinjava = TemplateUtil.create();
        xml = jinjava.render(xml, map);
        log.info("clone volume xml={}", xml);
        targetVol = targetStoragePool.storageVolCreateXMLFrom(xml, sourceVol, 0);
        storageVolInfo = LibvirtUtil.getVolInfo(targetVol);
        if(storageVolInfo.capacity< request.getSize()){
            targetVol.resize( request.getSize(), 0);
        }
        storageVolInfo = LibvirtUtil.getVolInfo(targetVol);
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

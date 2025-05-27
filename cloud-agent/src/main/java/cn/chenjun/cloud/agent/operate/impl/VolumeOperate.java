package cn.chenjun.cloud.agent.operate.impl;

import cn.chenjun.cloud.agent.util.CommandExecutor;
import cn.chenjun.cloud.agent.util.StorageUtil;
import cn.chenjun.cloud.common.bean.*;
import cn.chenjun.cloud.common.core.annotation.DispatchBind;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.libvirt.*;
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
public class VolumeOperate {


    private static String getVolumePath(Volume volume) {
        String path;
        switch (volume.getStorage().getType()) {
            case Constant.StorageType.NFS:
            case Constant.StorageType.GLUSTERFS:
                path = "/mnt/" + volume.getStorage().getName() + "/" + volume.getName();
                break;
            case Constant.StorageType.LOCAL:
                path = volume.getStorage().getMountPath() + "/" + volume.getName();
                break;
            case Constant.StorageType.CEPH_RBD:
                String pool = volume.getStorage().getParam().get("pool").toString();
                path = "rbd:" + pool + "/" + volume.getName();
                break;
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "未知的存储池类型:" + volume.getStorage().getType());
        }
        return path;
    }

    @DispatchBind(command = Constant.Command.VOLUME_INFO)

    public VolumeInfo getInfo(Connect connect, VolumeInfoRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getSourceStorage(), false);
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

    @DispatchBind(command = Constant.Command.LIST_STORAGE_VOLUME)
    public List<String> listStorageVolume(Connect connect, ListStorageVolumeRequest request) throws Exception {

        StoragePool storagePool = StorageUtil.findStorage(connect, request.getName(), false);
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_READY, "当前存储池未就绪:[" + request.getName() + "]");
        }
        return Arrays.asList(storagePool.listVolumes());
    }

    @DispatchBind(command = Constant.Command.DESTROY_UNLINK_VOLUME)
    public Void destroyUnlinkVolume(Connect connect, DestroyUnLinkVolumeRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getStorage(), false);
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_READY, "当前存储池未就绪:[" + request.getStorage() + "]");
        }
        for (String volume : request.getVolumes()) {
            StorageVol storageVol = this.findVol(storagePool, volume);
            if (storageVol != null) {
                storageVol.delete(0);
            }
        }
        return null;
    }
    @DispatchBind(command = Constant.Command.BATCH_VOLUME_INFO)
    public List<VolumeInfo> batchInfo(Connect connect, List<VolumeInfoRequest> batchRequest) throws Exception {
        Map<String, List<VolumeInfoRequest>> list = batchRequest.stream().collect(Collectors.groupingBy(VolumeInfoRequest::getSourceStorage));
        Map<String, Map<String, VolumeInfo>> result = new HashMap<>(4);
        for (Map.Entry<String, List<VolumeInfoRequest>> entry : list.entrySet()) {
            String storage = entry.getKey();
            Map<String, VolumeInfo> map = new HashMap<>(4);
            Set<String> volumeNameList = entry.getValue().stream().map(VolumeInfoRequest::getSourceName).collect(Collectors.toSet());
            StoragePool storagePool = StorageUtil.findStorage(connect, storage, false);
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

    public VolumeInfo create(Connect connect, VolumeCreateRequest request) throws Exception {
        String path = getVolumePath(request.getVolume());
        String[] commands = new String[]{"qemu-img", "create", "-f", request.getVolume().getType(), path, String.valueOf(request.getVolume().getCapacity())};
        CommandExecutor.CommandResult commandResult = CommandExecutor.executeCommand(commands);
        if (commandResult.getExitCode() == 0) {
            VolumeInfoRequest volumeInfoRequest = VolumeInfoRequest.builder().sourceStorage(request.getVolume().getStorage().getName()).sourceName(request.getVolume().getName()).build();
            return this.getInfo(connect, volumeInfoRequest);
        } else {
            throw new CodeException(ErrorCode.BASE_VOLUME_ERROR, "创建磁盘失败:" + commandResult.getError());
        }
    }

    @DispatchBind(command = Constant.Command.VOLUME_DESTROY)

    public Void destroy(Connect connect, VolumeDestroyRequest request) throws Exception {
        StoragePool storagePool = StorageUtil.findStorage(connect, request.getVolume().getStorage().getName(), false);
        if (storagePool == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND, "存储池未就绪:" + request.getVolume().getStorage().getName());
        }
        StorageVol storageVol = this.findVol(storagePool, request.getVolume().getName());
        if (storageVol != null) {
            storageVol.delete(0);
        }
        return null;
    }

    @DispatchBind(command = Constant.Command.VOLUME_RESIZE)

    public VolumeInfo resize(Connect connect, VolumeResizeRequest request) throws Exception {
        VolumeInfo sourceVolume = getInfo(connect, VolumeInfoRequest.builder().sourceStorage(request.getVolume().getStorage().getName()).sourceName(request.getVolume().getName()).build());
        if (sourceVolume.getCapacity() >= request.getSize()) {
            return sourceVolume;
        }
        StorageUtil.checkStorageSuccess(connect, request.getVolume().getStorage().getName());
        String path = getVolumePath(request.getVolume());
        String[] commands = new String[]{"qemu-img", "resize", path, String.valueOf(request.getSize())};
        CommandExecutor.CommandResult commandResult = CommandExecutor.executeCommand(commands);
        if (commandResult.getExitCode() == 0) {
            VolumeInfoRequest volumeInfoRequest = VolumeInfoRequest.builder().sourceStorage(request.getVolume().getStorage().getName()).sourceName(request.getVolume().getName()).build();

            VolumeInfo volumeInfo = this.getInfo(connect, volumeInfoRequest);
            if (!ObjectUtils.isEmpty(request.getVm())) {
                refreshDomainVolSize(connect, request.getVm(), volumeInfo.getName(), volumeInfo.getCapacity());
            }
            return volumeInfo;
        } else {
            throw new CodeException(ErrorCode.BASE_VOLUME_ERROR, "创建磁盘失败:" + commandResult.getError());
        }
    }


    @DispatchBind(command = Constant.Command.VOLUME_DOWNLOAD)

    public VolumeInfo download(Connect connect, VolumeDownloadRequest request) {
        StorageUtil.checkStorageSuccess(connect, request.getVolume().getStorage().getName());
        File tempFile = FileUtil.createTempFile();
        try {
            HttpUtil.downloadFile(request.getSourceUri(), tempFile, new StreamProgress() {
                int lastPercent = 0;


                public void start() {
                    log.info("开始下载文件:uri={},file={}", request.getSourceUri(), tempFile.getPath());
                }


                public void progress(long total, long progressSize) {
                    if (total > 0) {
                        int percent = (int) (progressSize * 100 / total);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            log.info("{} 下载进度 {}%", tempFile.getPath(), lastPercent);
                        }
                    }
                }


                public void finish() {
                    log.info("文件下载完毕:{}", tempFile.getPath());

                }
            });
            if (tempFile.length() <= 0) {
                throw new CodeException(ErrorCode.SERVER_ERROR, "文件下载错误，大小为0");
            }
            if (!ObjectUtils.isEmpty(request.getMd5())) {
                log.info("开始计算文件MD5:{}", tempFile.getPath());
                String md5 = DigestUtil.md5Hex(tempFile);
                log.info("文件md5:{}={}", md5, tempFile.getPath());
                if (!md5.equalsIgnoreCase(request.getMd5())) {
                    log.info("文件检验md5失败，下载失败.file={},fileMd5={},checkMd5={}", tempFile.getPath(), md5, request.getMd5());
                    throw new CodeException(ErrorCode.SERVER_ERROR, "文件下载MD5错误");
                }
                log.info("下载文件md5检验通过:{}", tempFile.getPath());
            }
            String[] commands = new String[]{"qemu-img", "convert", "-O", request.getVolume().getType(), tempFile.getPath(), getVolumePath(request.getVolume())};
            CommandExecutor.CommandResult commandResult = CommandExecutor.executeCommand(commands);
            if (commandResult.getExitCode() == 0) {
                VolumeInfoRequest volumeInfoRequest = VolumeInfoRequest.builder().sourceStorage(request.getVolume().getStorage().getName()).sourceName(request.getVolume().getName()).build();
                return getInfo(connect, volumeInfoRequest);
            } else {
                throw new CodeException(ErrorCode.BASE_VOLUME_ERROR, "创建磁盘失败:" + commandResult.getError());
            }
        } catch (Exception err) {
            log.error("下载导入模版出错.", err);
            throw new CodeException(ErrorCode.SERVER_ERROR, "下载文件出错:" + err.getMessage());
        } finally {
            FileUtil.del(tempFile);
        }
    }

    @DispatchBind(command = Constant.Command.VOLUME_MIGRATE)

    public VolumeInfo migrate(Connect connect, VolumeMigrateRequest request) throws Exception {
        return clone(connect, VolumeCloneRequest.builder()
                .sourceVolume(request.getSourceVolume())
                .targetVolume(request.getTargetVolume())
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

    public VolumeInfo clone(Connect connect, VolumeCloneRequest request) throws Exception {


        StorageUtil.checkStorageSuccess(connect, request.getSourceVolume().getStorage().getName());
        StorageUtil.checkStorageSuccess(connect, request.getTargetVolume().getStorage().getName());
        StoragePool targetStoragePool= StorageUtil.findStorage(connect, request.getTargetVolume().getStorage().getName(), false);
        StorageVol targetVol = findVol(targetStoragePool, request.getTargetVolume().getName());
        if(targetVol !=null){
            throw new CodeException(ErrorCode.VOLUME_EXISTS_ERROR, "目标磁盘已经存在:" + request.getTargetVolume().getName());
        }

        String sourcePath = getVolumePath(request.getSourceVolume());
        String targetPath = getVolumePath(request.getTargetVolume());
        String[] commands = new String[]{"qemu-img", "convert", "-O", request.getTargetVolume().getType(), sourcePath, targetPath};
        CommandExecutor.CommandResult commandResult = CommandExecutor.executeCommand(commands);
        if (commandResult.getExitCode() == 0) {
            VolumeResizeRequest volumeResizeRequest = VolumeResizeRequest.builder().volume(request.getTargetVolume()).size(request.getTargetVolume().getCapacity()).build();
            return resize(connect, volumeResizeRequest);
        } else {
            throw new CodeException(ErrorCode.BASE_VOLUME_ERROR, "创建磁盘失败:" + commandResult.getError());
        }

    }

    private void init(StorageVol vol, VolumeInfo info) throws Exception {
        String xml = vol.getXMLDesc(0);
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            Element formatNode = (Element) doc.selectSingleNode("/volume/target/format");
            if (formatNode != null) {
                info.setType(formatNode.attributeValue("type"));
            }
        }

    }

    private void refreshDomainVolSize(Connect connect, String vm, String volName, long capacity) {
        try {

            Domain domain = connect.domainLookupByName(vm);
            if (domain != null) {
                String xml = domain.getXMLDesc(0);
                String dev = "";
                try (StringReader sr = new StringReader(xml)) {
                    SAXReader reader = new SAXReader();
                    reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    Document doc = reader.read(sr);
                    List<Node> diskList = doc.selectNodes("/domain/devices/disk[@device='disk']");

                    for (Node node : diskList) {
                        Element element = (Element) node;
                        Element sourceElement = (Element) element.selectSingleNode("source");
                        String diskPath = sourceElement.attributeValue("file", "");
                        String namePath = sourceElement.attributeValue("name", "");
                        if (diskPath.endsWith("/" + volName) || namePath.endsWith(volName)) {
                            Element targetElement = (Element) element.selectSingleNode("target");
                            dev = targetElement.attributeValue("dev");
                            break;
                        }
                    }

                }
                if (!ObjectUtils.isEmpty(dev)) {
                    log.info("虚拟机磁盘刷新:{} dev:{} capacity:{}", vm, dev, capacity);
                    domain.blockResize(dev, capacity, Domain.BlockResizeFlags.BYTES);
                }
            }
        } catch (Exception ignored) {

        }
    }


}

package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.agent.operate.VolumeOperate;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.bean.*;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Constant;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class VolumeOperateImpl implements VolumeOperate {
    @Override
    public VolumeModel create(Connect connect, VolumeCreateRequest request) throws Exception {
        String xml;
        if (StringUtils.isEmpty(request.getParentVolume())) {
            xml = ResourceUtil.readUtf8Str("xml/volume/CreateVolume.xml");
            xml = String.format(xml, request.getTargetName(), request.getTargetSize(), request.getTargetSize(), request.getTargetVolume(), request.getTargetType());
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
            xml = String.format(xml, request.getTargetName(), request.getTargetSize(), request.getTargetSize(), request.getTargetVolume(), request.getTargetType(), request.getParentVolume(), request.getParentType());
        }
        FileUtil.mkParentDirs(request.getTargetVolume());
        StoragePool storagePool = connect.storagePoolLookupByName(request.getTargetStorage());
        StorageVol storageVol = storagePool.storageVolCreateXML(xml, 0);
        StorageVolInfo storageVolInfo = storageVol.getInfo();
        storagePool.refresh(0);
        return VolumeModel.builder().storage(request.getTargetStorage())
                .name(request.getTargetName())
                .path(storageVol.getPath())
                .type(storageVolInfo.type.toString())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
    }


    @Override
    public void destroy(Connect connect, VolumeDestroyRequest request) throws Exception {
        StoragePool storagePool = connect.storagePoolLookupByName(request.getSourceStorage());
        storagePool.refresh(0);
        String[] names=storagePool.listVolumes();
        for (String name : names) {
            StorageVol storageVol = storagePool.storageVolLookupByName(name);
            if(Objects.equals(storageVol.getPath(),request.getSourceVolume())){
                storageVol.delete(0);
                break;
            }
        }
    }

    @Override
    public VolumeModel clone(Connect connect, VolumeCloneRequest request) throws Exception {
        StoragePool sourceStoragePool = connect.storagePoolLookupByName(request.getSourceStorage());
        StoragePool targetStoragePool = connect.storagePoolLookupByName(request.getTargetStorage());
        sourceStoragePool.refresh(0);
        StorageVol sourceVol = this.findVol(sourceStoragePool, request.getSourceVolume());
        String xml = ResourceUtil.readUtf8Str("xml/volume/CloneVolume.xml");
        FileUtil.mkParentDirs(request.getTargetVolume());
        xml = String.format(xml, request.getTargetName(), request.getTargetVolume(), request.getTargetType());
        StorageVol targetVol = targetStoragePool.storageVolCreateXMLFrom(xml, sourceVol, 0);
        StorageVolInfo storageVolInfo = targetVol.getInfo();
        return VolumeModel.builder().storage(request.getTargetStorage())
                .name(request.getTargetName())
                .type(storageVolInfo.type.toString())
                .path(targetVol.getPath())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
    }

    @Override
    public VolumeModel resize(Connect connect, VolumeResizeRequest request) throws Exception {

        StoragePool storagePool = connect.storagePoolLookupByName(request.getSourceStorage());
        storagePool.refresh(0);
        StorageVol findVol = this.findVol(storagePool, request.getSourceVolume());
        if(findVol==null){
            throw  new CodeException(ErrorCode.SERVER_ERROR,"磁盘不存在:"+request.getSourceVolume());
        }
        findVol.resize(request.getSize(),1);
        StorageVolInfo storageVolInfo = findVol.getInfo();
        return VolumeModel.builder().storage(request.getSourceStorage())
                .name(findVol.getName())
                .type(storageVolInfo.type.toString())
                .path(findVol.getPath())
                .capacity(storageVolInfo.capacity)
                .allocation(storageVolInfo.allocation)
                .build();
    }

    @Override
    public VolumeModel snapshot(Connect connect, VolumeCreateSnapshotRequest request) throws Exception {
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
    public VolumeModel template(Connect connect, VolumeCreateTemplateRequest request) throws Exception {

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
    public VolumeModel download(Connect connect, VolumeDownloadRequest request) throws Exception {
        FileUtil.mkParentDirs(request.getTargetVolume());
        String tempFile=request.getTargetVolume()+".data";
        try {
            HttpUtil.downloadFile(request.getSourceUri(), new File(tempFile));
            return clone(connect, VolumeCloneRequest.builder()
                    .sourceStorage(request.getTargetStorage())
                    .sourceVolume(tempFile)
                    .targetName(request.getTargetName())
                    .targetStorage(request.getTargetStorage())
                    .targetVolume(request.getTargetVolume())
                    .targetType(request.getTargetType())
                    .build());
        }finally {
            FileUtil.del(tempFile);
        }
    }

    @Override
    public VolumeModel migrate(Connect connect, VolumeMigrateRequest request) throws Exception {
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
}

package cn.roamblue.cloud.agent.operate.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpUtil;
import cn.roamblue.cloud.agent.operate.VolumeOperate;
import cn.roamblue.cloud.common.agent.VolumeModel;
import cn.roamblue.cloud.common.agent.VolumeRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Objects;

/**
 * @author chenjun
 */
public class VolumeOperateImpl implements VolumeOperate {
    @Override
    public VolumeModel create(Connect connect, VolumeRequest.CreateVolume request) throws Exception {
        String xml;
        if(!StringUtils.isEmpty(request.getParentVolume())){
            xml=ResourceUtil.readUtf8Str("xml/volume/CloneVolume.xml");
            xml=String.format(xml,request.getTargetName(),request.getTargetSize(),request.getTargetSize(),request.getTargetVolume(),request.getTargetType());
        }else{
            xml=ResourceUtil.readUtf8Str("xml/volume/CreateVolumeByBackingStore.xml");
            xml=String.format(xml,request.getTargetName(),request.getTargetSize(),request.getTargetSize(),request.getTargetVolume(),request.getTargetType(),request.getParentVolume(),request.getParentType());
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
    public void destroy(Connect connect, VolumeRequest.DestroyVolume request) throws Exception {
        StoragePool storagePool = connect.storagePoolLookupByName(request.getSourceStorage());
        storagePool.refresh(0);
        String[] names=storagePool.listVolumes();
        for (String name : names) {
            StorageVol storageVol = storagePool.storageVolLookupByName(name);
            if(Objects.equals(storageVol.getPath(),request.getSourceVolume())){
                storageVol.wipe();
                storageVol.delete(0);
                break;
            }
        }
    }

    @Override
    public VolumeModel clone(Connect connect, VolumeRequest.CloneVolume request) throws Exception {
        StoragePool sourceStoragePool = connect.storagePoolLookupByName(request.getSourceStorage());
        StoragePool targetStoragePool = connect.storagePoolLookupByName(request.getTargetStorage());
        StorageVol sourceVol = sourceStoragePool.storageVolLookupByName(request.getSourceVolume());
        String xml= ResourceUtil.readUtf8Str("xml/volume/CloneVolume.xml");
        FileUtil.mkParentDirs(request.getTargetVolume());
        xml=String.format(xml,request.getTargetName(), request.getTargetVolume(),request.getTargetType());
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
    public VolumeModel resize(Connect connect, VolumeRequest.ResizeVolume request) throws Exception {

        StoragePool storagePool = connect.storagePoolLookupByName(request.getSourceStorage());
        storagePool.refresh(0);
        String[] names=storagePool.listVolumes();
        StorageVol findVol=null;
        for (String name : names) {
            StorageVol storageVol = storagePool.storageVolLookupByName(name);
            if (Objects.equals(storageVol.getPath(), request.getSourceVolume())) {
                findVol = storageVol;
                break;
            }
        }
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
    public VolumeModel snapshot(Connect connect, VolumeRequest.SnapshotVolume request) throws Exception {
        return clone(connect, VolumeRequest.CloneVolume.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceVolume(request.getSourceVolume())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetVolume(request.getTargetVolume())
                .targetType(request.getTargetType())
                .build());
    }

    @Override
    public VolumeModel template(Connect connect, VolumeRequest.TemplateVolume request) throws Exception {

        return clone(connect, VolumeRequest.CloneVolume.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceVolume(request.getSourceVolume())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetVolume(request.getTargetVolume())
                .targetType(request.getTargetType())
                .build());
    }

    @Override
    public VolumeModel download(Connect connect, VolumeRequest.DownloadVolume request) throws Exception {
        FileUtil.mkParentDirs(request.getTargetVolume());
        String tempFile=request.getTargetVolume()+".data";
        try {
            HttpUtil.downloadFile(request.getSourceUri(), new File(tempFile));
            return clone(connect, VolumeRequest.CloneVolume.builder()
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
    public VolumeModel migrate(Connect connect, VolumeRequest.MigrateVolume request) throws Exception {
        return clone(connect, VolumeRequest.CloneVolume.builder()
                .sourceStorage(request.getSourceStorage())
                .sourceVolume(request.getSourceVolume())
                .targetName(request.getTargetName())
                .targetStorage(request.getTargetStorage())
                .targetVolume(request.getTargetVolume())
                .targetType(request.getTargetType())
                .build());
    }

}

package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.operate.BaseOperateParam;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.ConfigKey;
import cn.chenjun.cloud.management.util.DomainUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
public abstract class AbstractOsOperate<T extends BaseOperateParam, V extends ResultUtil> extends AbstractOperate<T, V> {
    @Autowired
    protected ConfigService configService;

    protected String buildCdXml(GuestEntity guest, Map<String, Object> configParam) {
        if (guest.getCdRoom() <= 0) {
            return DomainUtil.buildCdXml((String) configParam.get(ConfigKey.VM_CD_NFS_TPL), configParam, null, null);
        }

        List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, guest.getCdRoom()));

        if (templateVolumeList.isEmpty()) {
            throw new CodeException(ErrorCode.TEMPLATE_NOT_READY, "光盘镜像未就绪");
        }

        TemplateVolumeEntity templateVolume = templateVolumeList.get(0);
        StorageEntity storage = storageMapper.selectById(templateVolume.getStorageId());

        if (storage == null) {
            throw new CodeException(ErrorCode.STORAGE_NOT_FOUND,
                    String.format("虚拟机[%s]光盘[%s]所属存储池不存在", guest.getDescription(), templateVolume.getName()));
        }

        if (storage.getStatus() != Constant.StorageStatus.READY) {
            throw new CodeException(ErrorCode.STORAGE_NOT_READY,
                    String.format("虚拟机[%s]光盘[%s]所属存储池未就绪:%s", guest.getDescription(), templateVolume.getName(), storage.getName()));
        }

        String configKey = getCdTplConfigKeyByStorageType(storage.getType());
        String tpl = (String) configParam.get(configKey);
        return DomainUtil.buildCdXml(tpl, configParam, storage, templateVolume);
    }

    private String getCdTplConfigKeyByStorageType(String storageType) {
        switch (storageType) {
            case Constant.StorageType.CEPH_RBD:
                return ConfigKey.VM_CD_CEPH_RBD_TPL;
            case Constant.StorageType.GLUSTERFS:
                return ConfigKey.VM_CD_GLUSTERFS_TPL;
            case Constant.StorageType.NFS:
                return ConfigKey.VM_CD_NFS_TPL;
            case Constant.StorageType.LOCAL:
                return ConfigKey.VM_CD_LOCAL_TPL;
            default:
                throw new CodeException(ErrorCode.STORAGE_NOT_SUPPORT, "不支持的存储池类型[" + storageType + "]");
        }
    }

    private String getDiskTplConfigKeyByStorageType(String storageType) {
        switch (storageType) {
            case Constant.StorageType.CEPH_RBD:
                return ConfigKey.VM_DISK_CEPH_RBD_TPL;
            case Constant.StorageType.GLUSTERFS:
                return ConfigKey.VM_DISK_GLUSTERFS_TPL;
            case Constant.StorageType.NFS:
                return ConfigKey.VM_DISK_NFS_TPL;
            case Constant.StorageType.LOCAL:
                return ConfigKey.VM_DISK_LOCAL_TPL;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型[" + storageType + "]");
        }
    }


    protected String buildDiskXml(GuestEntity guest, StorageEntity storage, VolumeEntity volume, int deviceId, String deviceType, Map<String, Object> sysconfig) {
        String configKey = getDiskTplConfigKeyByStorageType(storage.getType());
        String tpl = (String) sysconfig.get(configKey);
        return DomainUtil.buildDiskXml(tpl, sysconfig, guest, storage, volume, deviceId, deviceType);
    }
    protected String buildBlockDiskXml(GuestEntity guest,  VolumeEntity volume, int deviceId, String deviceType, Map<String, Object> sysconfig) {
        String tpl=(String) sysconfig.get(ConfigKey.VM_DISK_BLOCK_TPL);
        return DomainUtil.buildBlockDiskXml(tpl, sysconfig, guest,  volume, deviceId, deviceType);
    }

    protected String buildHostFileXml(GuestEntity guest, VolumeEntity volume, int deviceId, String deviceType, Map<String, Object> sysconfig) {
        String tpl = (String) sysconfig.get(ConfigKey.VM_DISK_FILE_TPL);
        return DomainUtil.buildHostFileXml(tpl, sysconfig, guest, volume, deviceId, deviceType);
    }
    public String buildInterfaceXml(NetworkEntity network, GuestNetworkEntity guestNetwork, Map<String, Object> systemConfig) {
        String tpl = (String) systemConfig.get(ConfigKey.VM_INTERFACE_TPL);
        return DomainUtil.buildNetworkInterfaceXml(tpl, systemConfig, network, guestNetwork);
    }
}

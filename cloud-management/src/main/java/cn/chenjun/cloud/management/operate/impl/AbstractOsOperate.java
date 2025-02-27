package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import cn.chenjun.cloud.management.servcie.ConfigService;
import cn.chenjun.cloud.management.util.DomainUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
public abstract class AbstractOsOperate<T extends BaseOperateParam, V extends ResultUtil> extends AbstractOperate<T, V> {
    @Autowired
    protected ConfigService configService;

    protected String getGuestCdRoom(GuestEntity guest, Map<String, Object> configParam) {
        TemplateVolumeEntity templateVolume = null;
        StorageEntity storage = null;
        if (guest.getCdRoom() > 0) {
            List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, guest.getCdRoom()));
            Collections.shuffle(templateVolumeList);
            if (!templateVolumeList.isEmpty()) {
                templateVolume = templateVolumeList.get(0);
                if (templateVolume != null) {
                    storage = this.storageMapper.selectById(templateVolume.getStorageId());
                    if (storage == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]光盘[" + templateVolume.getName() + "]所属存储池不存在");
                    }
                    if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]光盘[" + templateVolume.getName() + "]所属存储池未就绪:" + storage.getStatus());
                    }
                } else {
                    throw new CodeException(ErrorCode.TEMPLATE_NOT_READY, "光盘镜像未就绪");
                }
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "光盘镜像未就绪");
            }
        }
        String configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_CD_NFS_TPL;
        if (storage != null) {
            switch (storage.getType()) {
                case Constant.StorageType.CEPH_RBD:
                    configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_CD_CEPH_RBD_TPL;
                    break;
                case Constant.StorageType.GLUSTERFS:
                    configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_CD_GLUSTERFS_TPL;
                    break;
                case Constant.StorageType.NFS:
                    configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_CD_NFS_TPL;
                    break;
                default:
                    throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型[" + storage.getType() + "]");
            }
        }
        String tpl = (String) configParam.get(configKey);
        return DomainUtil.buildCdXml(tpl, configParam, storage, templateVolume);
    }
}

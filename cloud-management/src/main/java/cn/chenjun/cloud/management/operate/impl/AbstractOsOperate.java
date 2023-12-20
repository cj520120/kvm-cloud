package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.OsCdRoom;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.bean.Storage;
import cn.chenjun.cloud.common.bean.Volume;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.StorageEntity;
import cn.chenjun.cloud.management.data.entity.TemplateVolumeEntity;
import cn.chenjun.cloud.management.operate.bean.BaseOperateParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
public abstract class AbstractOsOperate<T extends BaseOperateParam, V extends ResultUtil> extends AbstractOperate<T, V> {
    protected OsCdRoom getGuestCdRoom(GuestEntity guest) {
        OsCdRoom cdRoom = OsCdRoom.builder().name(guest.getName()).build();
        if (guest.getCdRoom() > 0) {
            List<TemplateVolumeEntity> templateVolumeList = templateVolumeMapper.selectList(new QueryWrapper<TemplateVolumeEntity>().eq(TemplateVolumeEntity.TEMPLATE_ID, guest.getCdRoom()));
            Collections.shuffle(templateVolumeList);
            if (!templateVolumeList.isEmpty()) {
                TemplateVolumeEntity templateVolume = templateVolumeList.get(0);
                if (templateVolume != null) {
                    StorageEntity storageEntity = this.storageMapper.selectById(templateVolume.getStorageId());
                    if (storageEntity == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]光盘[" + templateVolume.getName() + "]所属存储池不存在");
                    }
                    if (storageEntity.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]光盘[" + templateVolume.getName() + "]所属存储池未就绪:" + storageEntity.getStatus());
                    }
                    Map<String, Object> storageParam = GsonBuilderUtil.create().fromJson(storageEntity.getParam(), new TypeToken<Map<String, Object>>() {
                    }.getType());
                    Storage storage = Storage.builder()
                            .name(storageEntity.getName())
                            .type(storageEntity.getType())
                            .param(storageParam)
                            .mountPath(storageEntity.getMountPath())
                            .build();
                    Volume cdVolume = Volume.builder().name(templateVolume.getName()).type(templateVolume.getType()).path(templateVolume.getPath()).storage(storage).build();
                    cdRoom.setVolume(cdVolume);
                } else {
                    throw new CodeException(ErrorCode.TEMPLATE_NOT_READY, "光盘镜像未就绪");
                }
            } else {
                throw new CodeException(ErrorCode.SERVER_ERROR, "光盘镜像未就绪");
            }
        }
        return cdRoom;
    }
}

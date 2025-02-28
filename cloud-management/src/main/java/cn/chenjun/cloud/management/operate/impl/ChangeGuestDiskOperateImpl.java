package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.ChangeGuestDiskRequest;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.operate.bean.ChangeGuestDiskOperate;
import cn.chenjun.cloud.management.servcie.bean.ConfigQuery;
import cn.chenjun.cloud.management.util.DomainUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 更改磁盘挂载
 *
 * @author chenjun
 */
@Component
@Slf4j
public class ChangeGuestDiskOperateImpl extends AbstractOperate<ChangeGuestDiskOperate, ResultUtil<Void>> {


    @Override
    public void operate(ChangeGuestDiskOperate param) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        switch (volume.getStatus()) {
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
            case cn.chenjun.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                GuestEntity guest = guestMapper.selectById(param.getGuestId());
                if (guest.getHostId() > 0) {
                    HostEntity host = hostMapper.selectById(guest.getHostId());
                    StorageEntity storage = this.storageMapper.selectById(volume.getStorageId());
                    if (storage == null) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池不存在");
                    }
                    if (storage.getStatus() != cn.chenjun.cloud.management.util.Constant.StorageStatus.READY) {
                        throw new CodeException(ErrorCode.SERVER_ERROR, "虚拟机[" + guest.getStatus() + "]磁盘[" + volume.getName() + "]所属存储池未就绪:" + storage.getStatus());
                    }
                    List<ConfigQuery> queryList = new ArrayList<>();
                    queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.DEFAULT).id(0).build());
                    queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.HOST).id(host.getHostId()).build());
                    queryList.add(ConfigQuery.builder().type(cn.chenjun.cloud.management.util.Constant.ConfigAllocateType.GUEST).id(guest.getGuestId()).build());
                    Map<String, Object> sysconfig = this.configService.loadSystemConfig(queryList);
                    String configKey;
                    switch (storage.getType()) {
                        case Constant.StorageType.CEPH_RBD:
                            configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_DISK_CEPH_RBD_TPL;
                            break;
                        case Constant.StorageType.GLUSTERFS:
                            configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_DISK_GLUSTERFS_TPL;
                            break;
                        case Constant.StorageType.NFS:
                            configKey = cn.chenjun.cloud.management.util.Constant.ConfigKey.VM_DISK_NFS_TPL;
                            break;
                        default:
                            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池类型[" + storage.getType() + "]");
                    }
                    String tpl = (String) sysconfig.get(configKey);
                    GuestDiskEntity guestDisk = GuestDiskEntity.builder().deviceId(param.getDeviceId()).build();
                    String xml = DomainUtil.buildDiskXml(tpl, sysconfig, guest, storage, volume, guestDisk);
                    ChangeGuestDiskRequest disk = ChangeGuestDiskRequest.builder().name(guest.getName()).xml(xml).build();
                    if (param.isAttach()) {

                        this.asyncInvoker(host, param, Constant.Command.GUEST_ATTACH_DISK, disk);
                    } else {
                        this.asyncInvoker(host, param, Constant.Command.GUEST_DETACH_DISK, disk);
                    }

                } else {
                    this.onSubmitFinishEvent(param.getTaskId(), ResultUtil.success());
                }
                break;
            default:
                throw new CodeException(ErrorCode.SERVER_ERROR, "磁盘[" + volume.getName() + "]状态不正确:" + volume.getStatus());

        }

    }

    @Override
    public Type getCallResultType() {
        return new TypeToken<ResultUtil<Void>>() {
        }.getType();
    }

    @Override
    public void onFinish(ChangeGuestDiskOperate param, ResultUtil<Void> resultUtil) {
        VolumeEntity volume = volumeMapper.selectById(param.getVolumeId());
        if (volume != null) {
            switch (volume.getStatus()) {
                case cn.chenjun.cloud.management.util.Constant.VolumeStatus.ATTACH_DISK:
                case cn.chenjun.cloud.management.util.Constant.VolumeStatus.DETACH_DISK:
                    volume.setStatus(cn.chenjun.cloud.management.util.Constant.VolumeStatus.READY);
                    volumeMapper.updateById(volume);
                    break;
                default:
                    break;
            }
        }
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_GUEST).build());
        this.notifyService.publish(NotifyData.<Void>builder().id(param.getGuestId()).type(Constant.NotifyType.UPDATE_VOLUME).build());
    }

    @Override
    public int getType() {
        return cn.chenjun.cloud.management.util.Constant.OperateType.CHANGE_GUEST_DISK;
    }
}

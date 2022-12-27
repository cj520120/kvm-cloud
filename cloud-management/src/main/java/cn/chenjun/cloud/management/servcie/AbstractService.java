package cn.chenjun.cloud.management.servcie;

import cn.chenjun.cloud.management.component.VncService;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.chenjun.cloud.management.data.entity.*;
import cn.chenjun.cloud.management.data.mapper.*;
import cn.chenjun.cloud.management.model.*;
import cn.chenjun.cloud.management.task.OperateTask;
import cn.hutool.core.convert.impl.BeanConverter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

public abstract class AbstractService {
    @Autowired
    protected GuestMapper guestMapper;
    @Autowired
    protected StorageMapper storageMapper;
    @Autowired
    protected GuestNetworkMapper guestNetworkMapper;
    @Autowired
    protected HostMapper hostMapper;
    @Autowired
    protected VolumeMapper volumeMapper;
    @Autowired
    protected SnapshotVolumeMapper snapshotVolumeMapper;
    @Autowired
    protected GuestDiskMapper guestDiskMapper;
    @Autowired
    protected NetworkMapper networkMapper;
    @Autowired
    protected TemplateMapper templateMapper;
    @Autowired
    protected TemplateVolumeMapper templateVolumeMapper;
    @Autowired
    @Lazy
    protected OperateTask operateTask;
    @Autowired
    protected ApplicationConfig applicationConfig;
    @Autowired
    protected GuestVncMapper guestVncMapper;
    @Autowired
    @Lazy
    protected VncService vncService;
    @Autowired
    protected SchemeMapper schemeMapper;
    @Autowired
    protected NotifyService notifyService;

    protected StorageModel initStorageModel(StorageEntity entity) {
        return new BeanConverter<>(StorageModel.class).convert(entity, null);
    }

    protected SchemeModel initScheme(SchemeEntity entity) {
        return new BeanConverter<>(SchemeModel.class).convert(entity, null);
    }

    protected VolumeModel initVolume(GuestDiskEntity disk) {
        VolumeModel model = new BeanConverter<>(VolumeModel.class).convert(volumeMapper.selectById(disk.getVolumeId()), null);
        model.setAttach(VolumeAttachModel.builder().guestId(disk.getGuestId()).deviceId(disk.getDeviceId()).guestDiskId(disk.getGuestDiskId()).build());
        return model;
    }

    protected NetworkModel initGuestNetwork(NetworkEntity entity) {
        return new BeanConverter<>(NetworkModel.class).convert(entity, null);
    }

    protected GuestNetworkModel initGuestNetwork(GuestNetworkEntity entity) {
        return new BeanConverter<>(GuestNetworkModel.class).convert(entity, null);
    }

    protected HostModel initHost(HostEntity entity) {

        entity.setTotalCpu((int) (entity.getTotalCpu() * applicationConfig.getOverCpu()));
        entity.setTotalMemory((long) (entity.getTotalMemory() * applicationConfig.getOverMemory()));
        return new BeanConverter<>(HostModel.class).convert(entity, null);
    }

    protected TemplateModel initTemplateModel(TemplateEntity entity) {
        return new BeanConverter<>(TemplateModel.class).convert(entity, null);

    }

    protected VolumeModel initVolume(VolumeEntity volume) {
        VolumeModel model = new BeanConverter<>(VolumeModel.class).convert(volume, null);
        GuestDiskEntity disk = this.guestDiskMapper.selectOne(new QueryWrapper<GuestDiskEntity>().eq("volume_id", volume.getVolumeId()));
        if (disk != null && disk.getGuestId() != 0) {
            GuestEntity guest = this.guestMapper.selectById(disk.getGuestId());
            if (guest != null) {
                model.setAttach(VolumeAttachModel.builder().guestId(disk.getGuestId()).deviceId(disk.getDeviceId()).description(guest.getDescription()).guestDiskId(disk.getGuestDiskId()).build());
            }
        }
        return model;
    }

    protected SnapshotModel initSnapshot(SnapshotVolumeEntity volume) {
        SnapshotModel model = new BeanConverter<>(SnapshotModel.class).convert(volume, null);

        return model;
    }
}

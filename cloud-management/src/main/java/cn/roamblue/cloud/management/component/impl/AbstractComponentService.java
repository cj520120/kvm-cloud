package cn.roamblue.cloud.management.component.impl;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.Lock;
import cn.roamblue.cloud.management.component.ComponentService;
import cn.roamblue.cloud.management.data.entity.*;
import cn.roamblue.cloud.management.data.mapper.ComponentMapper;
import cn.roamblue.cloud.management.data.mapper.GuestMapper;
import cn.roamblue.cloud.management.data.mapper.GuestNetworkMapper;
import cn.roamblue.cloud.management.data.mapper.TemplateMapper;
import cn.roamblue.cloud.management.model.GuestModel;
import cn.roamblue.cloud.management.servcie.AllocateService;
import cn.roamblue.cloud.management.servcie.GuestService;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class AbstractComponentService implements ComponentService {
    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private GuestNetworkMapper guestNetworkMapper;
    @Autowired
    private GuestMapper guestMapper;
    @Autowired
    private GuestService guestService;
    @Autowired
    private AllocateService allocateService;
    @Autowired
    private TemplateMapper templateMapper;

    @Lock(RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void init(int networkId) {
        try {
            List<ComponentEntity> componentList = componentMapper.selectList(new QueryWrapper<ComponentEntity>().eq("network_id", networkId).last("limit 0,1"));
            while (componentList.size() > 1) {
                for (ComponentEntity component : componentList) {
                    guestService.destroyGuest(component.getGuestId());
                    componentMapper.deleteById(component.getComponentId());
                }
            }
            if (componentList.isEmpty()) {
                List<TemplateEntity> templateList = this.templateMapper.selectList(new QueryWrapper<TemplateEntity>().eq("template_type", Constant.TemplateType.SYSTEM).eq("template_status", Constant.TemplateStatus.READY));
                if (templateList.isEmpty()) {
                    return;
                }
                Collections.shuffle(templateList);
                int diskTemplateId = templateList.get(0).getTemplateId();
                HostEntity host = this.allocateService.allocateHost(0, 0, 1, 500);
                StorageEntity storage = this.allocateService.allocateStorage(0);
                GuestModel guestModel = this.guestService.createGuest(
                        Constant.GuestType.SYSTEM, "Route", cn.roamblue.cloud.common.util.Constant.DiskBus.VIRTIO
                        , host.getHostId(), 1, 500, networkId, cn.roamblue.cloud.common.util.Constant.NetworkDriver.VIRTIO,
                        0, diskTemplateId, 0, 0,
                        storage.getStorageId(), cn.roamblue.cloud.common.util.Constant.VolumeType.QCOW2, 0, true).getData();

                componentMapper.insert(ComponentEntity.builder().guestId(guestModel.getGuestId()).componentType(this.getType()).networkId(networkId).build());
            } else {
                ComponentEntity component = componentList.get(0);
                GuestEntity guest = guestMapper.selectById(component.getGuestId());
                if (guest == null) {
                    componentMapper.deleteById(component.getComponentId());
                    return;
                }
                if (guest.getStatus() == Constant.GuestStatus.STOP) {
                    //重新启动
                    HostEntity host = this.allocateService.allocateHost(0, 0, 1, 500);
                    this.guestService.start(guest.getGuestId(), host.getHostId());
                } else if (guest.getStatus() == Constant.GuestStatus.ERROR) {
                    //重新启动
                    this.guestService.destroyGuest(guest.getGuestId());
                }
            }
        } catch (CodeException err) {
            switch (err.getCode()) {
                case ErrorCode.STORAGE_NOT_FOUND:
                case ErrorCode.STORAGE_NOT_SPACE:
                case ErrorCode.HOST_NOT_SPACE:
                case ErrorCode.HOST_NOT_FOUND:
                    break;
            }
        } catch (Exception err) {
            log.error("Component {} init fail.", this.getName(), err);
        }
    }

    protected abstract String getName();
}

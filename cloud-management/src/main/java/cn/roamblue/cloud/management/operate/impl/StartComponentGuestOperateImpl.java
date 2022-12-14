package cn.roamblue.cloud.management.operate.impl;

import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.common.bean.OsNic;
import cn.roamblue.cloud.management.data.entity.ComponentEntity;
import cn.roamblue.cloud.management.data.entity.GuestEntity;
import cn.roamblue.cloud.management.data.mapper.ComponentMapper;
import cn.roamblue.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.roamblue.cloud.management.servcie.ComponentService;
import cn.roamblue.cloud.management.util.Constant;
import cn.roamblue.cloud.management.util.IpCaculate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 启动虚拟机
 *
 * @author chenjun
 */
@Component
@Slf4j
public class StartComponentGuestOperateImpl extends StartGuestOperateImpl<StartComponentGuestOperate> {

    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private List<ComponentService> componentServices;

    public StartComponentGuestOperateImpl() {
        super( StartComponentGuestOperate.class);
    }

    @Override
    protected List<OsNic> getGuestNetwork(GuestEntity guest) {
        List<OsNic> nicList= super.getGuestNetwork(guest);
        ComponentEntity component= componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id",guest.getGuestId()));
        if(component!=null&&Objects.equals(component.getComponentType(), Constant.ComponentType.ROUTE)){
            for (OsNic osNic : nicList) {
                osNic.setDeviceId(osNic.getDeviceId()+1);
            }
            if(!nicList.isEmpty()) {
                OsNic metaServiceNic = OsNic.builder()
                        .deviceId(0)
                        .name("")
                        .mac(IpCaculate.getMacAddrWithFormat(":"))
                        .bridgeName(nicList.get(0).getBridgeName())
                        .driveType(cn.roamblue.cloud.common.util.Constant.NetworkDriver.VIRTIO)
                        .build();
                nicList.add(0,metaServiceNic);
            }
        }
        return nicList;
    }

    @Override
    protected GuestQmaRequest getQmaRequest(GuestEntity guest) {
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guest.getGuestId()));
        Optional<ComponentService> componentService = componentServices.stream().filter(t -> Objects.equals(t.getComponentType(), component.getComponentType())).findFirst();
        if (componentService.isPresent()) {
            return componentService.get().getQmaRequest(guest.getGuestId());
        }
        return null;
    }
}
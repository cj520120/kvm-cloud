package cn.chenjun.cloud.management.operate.impl;

import cn.chenjun.cloud.common.bean.GuestInfo;
import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.common.bean.OsNic;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.management.annotation.Lock;
import cn.chenjun.cloud.management.component.AbstractComponentService;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.data.mapper.ComponentMapper;
import cn.chenjun.cloud.management.operate.bean.StartComponentGuestOperate;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.IpCaculate;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenjun
 */
@Component
@Slf4j
public class StartComponentGuestOperateImpl extends StartGuestOperateImpl<StartComponentGuestOperate> {

    @Autowired
    private ComponentMapper componentMapper;
    @Autowired
    private List<AbstractComponentService> componentServices;

    public StartComponentGuestOperateImpl() {
        super(StartComponentGuestOperate.class);
    }

    @Override
    protected List<OsNic> getGuestNetwork(GuestEntity guest) {
        List<OsNic> nicList = super.getGuestNetwork(guest);

        NetworkEntity network = this.networkMapper.selectById(guest.getNetworkId());
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guest.getGuestId()));
        if (component != null && Objects.equals(component.getComponentType(), Constant.ComponentType.ROUTE)) {
            for (OsNic osNic : nicList) {
                osNic.setDeviceId(osNic.getDeviceId());
            }
            //如果是vlan，则写入网关地址的网卡
            if (Objects.equals(network.getType(), Constant.NetworkType.VLAN)) {
                for (OsNic osNic : nicList) {
                    osNic.setDeviceId(osNic.getDeviceId() + 1);
                }
                //将网关地址设置为0号网卡
                OsNic metaServiceNic = OsNic.builder()
                        .deviceId(0)
                        .name("")
                        .mac(IpCaculate.getMacAddrWithFormat(":"))
                        .bridgeName(network.getBridge())
                        .driveType(cn.chenjun.cloud.common.util.Constant.NetworkDriver.VIRTIO)
                        .vlanId(network.getVlanId())
                        .build();
                nicList.add(0, metaServiceNic);
            }
        }
        return nicList;
    }

    @Override
    protected GuestQmaRequest getQmaRequest(GuestEntity guest) {
        ComponentEntity component = componentMapper.selectOne(new QueryWrapper<ComponentEntity>().eq("guest_id", guest.getGuestId()));
        Optional<AbstractComponentService> componentService = componentServices.stream().filter(t -> Objects.equals(t.getComponentType(), component.getComponentType())).findFirst();
        if (componentService.isPresent()) {
            return componentService.get().getQmaRequest(guest.getGuestId());
        }
        return null;
    }

    @Lock(value = RedisKeyUtil.GLOBAL_LOCK_KEY)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onFinish(StartComponentGuestOperate param, ResultUtil<GuestInfo> resultUtil) {
        super.onFinish(param, resultUtil);

    }
}
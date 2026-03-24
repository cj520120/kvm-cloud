package cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.nat;

import cn.chenjun.cloud.common.util.Constant;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;
import cn.chenjun.cloud.management.operate.impl.cloud.impl.component.impl.BaseInitialization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class NatFinishInitialization extends BaseInitialization {
    @Override
    public boolean isSupport(int componentType) {
        return Objects.equals(componentType, Constant.ComponentType.NAT);
    }

    @Override
    public void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest) {


    }

    @Override
    public int getOrder() {
        return 100;
    }
}

package cn.chenjun.cloud.management.operate.impl.cloud.impl.component;

import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.ComponentGuestEntity;
import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import cn.chenjun.cloud.management.operate.impl.cloud.bean.CloudConfig;

public interface ComponentInitialization {

    boolean isSupport(int componentType);

    void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest);

    int getOrder();

}

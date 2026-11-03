package cn.ruiyun.cloud.management.operate.impl.cloud.impl.component;

import cn.ruiyun.cloud.management.data.entity.ComponentEntity;
import cn.ruiyun.cloud.management.data.entity.ComponentGuestEntity;
import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.data.entity.NetworkEntity;
import cn.ruiyun.cloud.management.operate.impl.cloud.bean.CloudConfig;

public interface ComponentInitialization {

    boolean isSupport(int componentType);

    void initialize(CloudConfig config, GuestEntity guest, NetworkEntity network, ComponentEntity component, ComponentGuestEntity componentGuest);

    int getOrder();

}

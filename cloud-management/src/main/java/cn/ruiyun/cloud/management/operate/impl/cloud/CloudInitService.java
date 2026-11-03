package cn.ruiyun.cloud.management.operate.impl.cloud;

import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.data.entity.HostEntity;
import cn.ruiyun.cloud.management.operate.impl.cloud.bean.CloudData;

public interface CloudInitService {
    /**
     * 构建云初始化数据
     *
     * @param guest
     * @param host
     * @return
     */
    CloudData build(GuestEntity guest, HostEntity host);

    /**
     * 获取支持的虚拟机类型
     *
     * @return
     */
    int getSupportType();
}

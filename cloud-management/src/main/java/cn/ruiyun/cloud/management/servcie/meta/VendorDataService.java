package cn.ruiyun.cloud.management.servcie.meta;

import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.servcie.bean.MetaData;
import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface VendorDataService extends Plugin<GuestEntity> {

    /**
     * 获取用户数据
     *
     * @param guest
     * @return
     */
    MetaData load(GuestEntity guest);
}

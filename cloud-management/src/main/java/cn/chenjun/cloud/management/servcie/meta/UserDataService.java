package cn.chenjun.cloud.management.servcie.meta;

import cn.chenjun.cloud.management.data.entity.GuestEntity;
import cn.chenjun.cloud.management.servcie.bean.MetaData;
import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface UserDataService extends Plugin<GuestEntity> {
    /**
     * 获取meta数据
     *
     * @param guest
     * @return
     */
    MetaData load(GuestEntity guest);
}

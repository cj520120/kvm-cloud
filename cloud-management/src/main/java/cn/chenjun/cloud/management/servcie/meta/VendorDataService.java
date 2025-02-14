package cn.chenjun.cloud.management.servcie.meta;

import cn.chenjun.cloud.management.servcie.bean.MetaData;
import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface VendorDataService extends Plugin<Integer> {

    /**
     * 获取用户数据
     * @param guestId
     * @return
     */
    MetaData load(int guestId);
}

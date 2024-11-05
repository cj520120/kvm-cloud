package cn.chenjun.cloud.management.servcie.meta;

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
    String loadVendorData(int guestId);
}

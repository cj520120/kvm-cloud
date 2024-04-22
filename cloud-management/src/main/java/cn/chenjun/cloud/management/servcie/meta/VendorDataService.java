package cn.chenjun.cloud.management.servcie.meta;

import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface VendorDataService extends Plugin<Integer> {

    /**
     * @param guestId
     * @return
     */
    String loadVendorData(int guestId);
}

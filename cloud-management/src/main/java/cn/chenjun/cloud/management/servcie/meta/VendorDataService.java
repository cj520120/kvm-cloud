package cn.chenjun.cloud.management.servcie.meta;

/**
 * @author chenjun
 */
public interface VendorDataService {

    /**
     * 获取用户数据
     * @param guestId
     * @return
     */
    String loadVendorData(int guestId);
}

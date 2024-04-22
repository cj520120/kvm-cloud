package cn.chenjun.cloud.management.servcie.meta.impl;

import cn.chenjun.cloud.common.util.SystemCategory;
import cn.chenjun.cloud.management.servcie.meta.VendorDataService;
import org.springframework.stereotype.Service;

/**
 * @author chenjun
 */
@Service
public class DefaultVendorDataService implements VendorDataService {
    @Override
    public String loadVendorData(int guestId) {
        return "bootcmd:\n - echo ----------complete-------------";
    }

    @Override
    public boolean supports(Integer systemCategory) {
        switch (systemCategory) {
            case SystemCategory.WINDOWS:
            case SystemCategory.ANDROID:
                return false;
            default:
                return true;

        }
    }
}

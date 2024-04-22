package cn.chenjun.cloud.management.servcie.meta;

import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface UserDataService extends Plugin<Integer> {
    /**
     * 获取meta数据
     *
     * @param guestId
     * @return
     */
    String loadUserData(int guestId);
}

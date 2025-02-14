package cn.chenjun.cloud.management.servcie.meta;

import cn.chenjun.cloud.management.servcie.bean.MetaData;
import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface MetaDataService extends Plugin<Integer> {
    /**
     * 获取meta数据
     *
     * @param guestId
     * @return
     */
    MetaData buildCloudInitMetaData(int guestId);

    /**
     * 获取meta数据
     *
     * @param guestId
     * @param key
     * @return
     */
    String findMetaDataByKey(int guestId, String key);
}

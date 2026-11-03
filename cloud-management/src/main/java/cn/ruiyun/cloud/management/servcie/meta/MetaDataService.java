package cn.ruiyun.cloud.management.servcie.meta;

import cn.ruiyun.cloud.management.data.entity.GuestEntity;
import cn.ruiyun.cloud.management.servcie.bean.MetaData;
import org.springframework.plugin.core.Plugin;

import java.util.List;

/**
 * @author chenjun
 */
public interface MetaDataService extends Plugin<GuestEntity> {
    /**
     * 获取meta数据
     *
     * @param guest
     * @return
     */
    MetaData buildCloudInitMetaData(GuestEntity guest);

    /**
     * 获取meta数据
     *
     * @param guest
     * @return
     */
    List<String> listMetaDataKeys(GuestEntity guest);

    /**
     * 获取meta数据
     *
     * @param guest
     * @param key
     * @return
     */
    String findMetaDataByKey(GuestEntity guest, String key);
}

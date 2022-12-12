package cn.roamblue.cloud.management.component;

import cn.roamblue.cloud.common.bean.GuestQmaRequest;
import cn.roamblue.cloud.common.bean.OsNic;

import java.util.List;

/**
 * @author chenjun
 */
public interface ComponentService {
    /**
     * 组件类型
     *
     * @return
     */
    int getType();

    /**
     * 组件初始化
     *
     * @param networkId
     */
    void init(int networkId);

    /**
     * 获取qma信息
     * @param guestId
     * @param networkId
     * @return
     */
    GuestQmaRequest getStartQmaRequest(int guestId, int networkId);

    /**
     * 获取网络配置
     * @param guestId
     * @param networkId
     * @return
     */
    List<OsNic> getDefaultNic(int guestId, int networkId);
}

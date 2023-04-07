package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;

import java.util.List;

/**
 * @author chenjun
 */
public interface RouteInitialize {

    /**
     * 初始化route
     *
     * @param guestId
     * @return
     */
    List<GuestQmaRequest.QmaBody> initialize(int guestId);

    /**
     * 是否支持meta服务
     *
     * @return
     */
    boolean isEnableMetaService();
}

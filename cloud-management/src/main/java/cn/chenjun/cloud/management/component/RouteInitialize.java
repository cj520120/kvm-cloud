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
}

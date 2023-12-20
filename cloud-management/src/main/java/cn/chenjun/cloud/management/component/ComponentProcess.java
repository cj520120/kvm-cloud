package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import org.springframework.plugin.core.Plugin;

/**
 * @author chenjun
 */
public interface ComponentProcess extends Plugin<Integer> {
    /**
     * 检查并启动组件
     *
     * @param network
     * @param component
     */
    void checkAndStart(NetworkEntity network, ComponentEntity component);

    /**
     * 获取组件启动脚本
     *
     * @param component
     * @param guestId
     * @return
     */
    GuestQmaRequest getStartQmaRequest(ComponentEntity component, int guestId);
}

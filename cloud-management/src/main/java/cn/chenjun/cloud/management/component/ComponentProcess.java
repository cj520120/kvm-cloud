package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.entity.NetworkEntity;
import org.springframework.plugin.core.Plugin;

import java.util.Map;

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
    boolean checkAndStart(NetworkEntity network, ComponentEntity component, HostEntity host, boolean isMaster);

    /**
     * 清理主机上的组件
     * @param component
     * @param host
     */
    void cleanHostComponent(ComponentEntity component, HostEntity host);
    /**
     * 获取组件启动脚本
     *
     * @param component
     * @param guestId
     * @return
     */
    GuestQmaRequest getStartQmaRequest(ComponentEntity component, int guestId, Map<String, Object> sysconfig);

    /**
     * 获取组件组件名称
     *
     * @return
     */
    String getComponentName();
}

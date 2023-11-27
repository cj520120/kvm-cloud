package cn.chenjun.cloud.management.component;

import cn.chenjun.cloud.common.bean.GuestQmaRequest;
import cn.chenjun.cloud.management.data.entity.ComponentEntity;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * @author chenjun
 */
public interface ComponentQmaInitialize extends Ordered {

    /**
     * 初始化脚本
     *
     * @param component
     * @return
     */
    List<GuestQmaRequest.QmaBody> initialize(ComponentEntity component);

}

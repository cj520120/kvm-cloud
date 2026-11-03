package cn.ruiyun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeGuestInterfaceRequest {
    /**
     * 虚拟机名称
     */
    private String name;
    private NetworkNic nic;
}

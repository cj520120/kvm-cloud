package cn.chenjun.cloud.common.bean;

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
public class GuestShutdownRequest {
    /**
     * 虚拟机名称
     */
    private String name;
    /**
     * 等待超时时间
     */
    private long expire;

}

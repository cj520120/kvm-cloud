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
public class OsDisk {
    /**
     * 虚拟机名称
     */
    private String name;
    private int deviceId;
    private String volume;
    private String volumeType;

}

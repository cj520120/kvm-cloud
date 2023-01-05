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
public class OsNic {
    /**
     * 虚拟机名称
     */
    private String name;
    private String driveType;
    private int deviceId;
    private String mac;
    private String bridgeName;
}

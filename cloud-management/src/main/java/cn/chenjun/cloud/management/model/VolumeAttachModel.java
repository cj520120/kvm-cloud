package cn.chenjun.cloud.management.model;

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
public class VolumeAttachModel {
    private int guestDiskId;
    private int guestId;
    private int deviceId;
    private String deviceBus;
}

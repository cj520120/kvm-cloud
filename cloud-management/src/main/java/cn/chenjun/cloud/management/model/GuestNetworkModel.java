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
public class GuestNetworkModel {
    private int guestNetworkId;
    private int guestId;
    private int networkId;
    private int deviceId;
    private String driveType;
    private String mac;
    private String ip;
}

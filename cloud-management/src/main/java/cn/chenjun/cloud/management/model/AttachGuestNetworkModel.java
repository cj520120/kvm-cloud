package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachGuestNetworkModel {
    private GuestModel guest;
    private GuestNetworkModel network;
}
